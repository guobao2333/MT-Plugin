package bin.mt.plugin.demo;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bin.mt.json.JSONArray;
import bin.mt.plugin.api.translation.BaseBatchTranslationEngine;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GoogleTranslationEngine extends BaseBatchTranslationEngine {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

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

    @Override
    public BatchingStrategy createBatchingStrategy() {
        // 实际限制5000，留点余量
        return new DefaultBatchingStrategy(100, 4500) {
            @Override
            protected int getTextDataSize(String text) {
                return text.length() + 10; // 预留分割线大小
            }
        };
    }

    /**
     * 调用内置的批量翻译桥接方法：将多条文本合并为一次单文本翻译请求，再按分隔线拆分结果。
     */
    @NonNull
    @Override
    public String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage) throws IOException {
        return batchTranslateBySingleTranslate(texts, sourceLanguage, targetLanguage);
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