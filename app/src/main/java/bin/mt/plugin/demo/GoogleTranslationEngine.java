package bin.mt.plugin.demo;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import bin.mt.json.JSONArray;
import bin.mt.plugin.api.translation.BaseBatchTranslationEngine;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GoogleTranslationEngine extends BaseBatchTranslationEngine {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    private final List<String> sourceLanguages = Arrays.asList("auto", "zh", "en", "ru");

    private final List<String> targetLanguages = Arrays.asList("zh", "en", "ru");


    @NonNull
    @Override
    public String name() {
        return "{google_translator}";
    }

    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return sourceLanguages;
    }

    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return targetLanguages;
    }

    @Override
    public int getMaxBatchSize() {
        return 100;
    }

    @Override
    public int getMaxBatchCharacters() {
        return 4000;
    }

    /**
     * 由于我们使用的谷歌翻译接口只能翻译单个文本，因此这里使用分割线组合文本，翻译完再拆分
     */
    @NonNull
    @Override
    public String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage) throws IOException {
        // 生成一个分割线，确保原文里面没有
        String divider = "--------";
        while (containsDivider(texts, divider)) {
            divider += "--";
        }

        boolean tryAgain = true;

        while (true) {
            // 拼接、翻译、分割
            String originalText = Arrays.stream(texts).map(String::trim).collect(Collectors.joining("\n" + divider + "\n"));
            String translatedText = translate(originalText, sourceLanguage, targetLanguage);
            String[] array = translatedText.split("\n" + divider + "\n");

            // 确保前后数量一致
            if (array.length == texts.length) {
                return array;
            }

            // 增加分割线长度再试一次
            if (tryAgain) {
                tryAgain = false;
                //noinspection StringConcatenationInLoop
                divider += "--";
                continue;
            }

            // 输出错误数据
            getContext().log("Original(" + texts.length + "):\n" + originalText);
            getContext().log("Translated(" + array.length + "):\n" + translatedText);
            if (translatedText.length() > 100) {
                translatedText = translatedText.substring(0, 99) + "...";
            }
            throw new IOException("Translation failed: " + translatedText);
        }
    }

    private static boolean containsDivider(String[] texts, String divider) {
        for (String s : texts) {
            if (s.contains(divider)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        // 142.250.0.160
        // 142.250.0.161
        // 142.250.0.183
        // 142.250.0.184
        // 172.217.4.108
        // 142.250.4.160
        String url = "http://142.250.0.160/translate_a/single?client=gtx&dt=t" +
                "&sl=" + sourceLanguage +
                "&tl=" + targetLanguage +
                "&q=" + URLEncoder.encode(text, "UTF-8");
        Request request = new Request.Builder()
                .get()
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