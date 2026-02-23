package bin.mt.plugin.demo;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bin.mt.json.JSONArray;
import bin.mt.plugin.api.translation.BaseTranslationEngine;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GoogleTranslationEngine extends BaseTranslationEngine {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onBuildConfiguration(ConfigurationBuilder builder) {
        super.onBuildConfiguration(builder);
        // 开启批量翻译功能（用于翻译模式功能）
        builder.setAllowBatchTranslationBySeparator(true);
        // 设置单次翻译最大文本长度，MT会自动拆分
        builder.setMaxTranslationTextLength(5000);
    }

    @NonNull
    @Override
    public String name() {
        return "{google_translator}";
    }

    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return List.of("auto", "zh", "en", "ru");
    }

    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return List.of("zh", "en", "ru");
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        // 中国大陆不支持直接访问 translate.googleapis.com，这里使用 IP 直连
        // 142.250.0.160  142.250.0.161  142.250.0.183
        // 142.250.0.184  172.217.4.108  142.250.4.160
        String url = "http://142.250.0.160/translate_a/single";
        FormBody formBody = new FormBody.Builder()
                .add("client", "gtx")
                .add("dt", "t")
                .add("sl", sourceLanguage)
                .add("tl", targetLanguage)
                .add("q", text)
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .header("Host", "translate.googleapis.com")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP response code: " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Body is null");
            }
            return getResult(body.string());
        }
    }

    private static String getResult(String string) throws IOException {
        if (!string.startsWith("[[[")) {
            throw new IOException("Parse result failed: " + string);
        }
        JSONArray array = new JSONArray(string).getJSONArray(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            sb.append(array.getJSONArray(i).getString(0));
        }
        return sb.toString();
    }
}