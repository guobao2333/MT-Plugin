package bin.mt.plugin;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import bin.mt.json.JSONArray;
import bin.mt.json.JSONObject;
import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DeepSeekTranslationEngine extends BaseTranslationEngine {
    private static final MediaType JSON_MEDIA_TYPE = Objects.requireNonNull(MediaType.parse("application/json; charset=utf-8"));

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(25, TimeUnit.SECONDS)
            .build();

    private final List<String> sourceLanguages = Arrays.asList("auto",
            "zh", "zh-TW", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca",
            "ceb", "co", "cs", "cy", "da", "de", "el", "eo", "es", "et", "eu", "fa",
            "fi", "fr", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "hi", "hmn", "hr",
            "ht", "hu", "hy", "id", "ig", "is", "it", "iw", "ja", "jw", "ka", "kk",
            "km", "kn", "ko", "ku", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi",
            "mk", "ml", "mn", "mr", "ms", "mt", "my", "ne", "nl", "no", "ny", "pa",
            "pl", "ps", "pt", "ro", "ru", "sd", "si", "sk", "sl", "sm", "sn", "so",
            "sq", "sr", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr",
            "ug", "uk", "ur", "uz", "vi", "xh", "yi", "yo", "zu");

    private final List<String> targetLanguages = sourceLanguages.subList(1, sourceLanguages.size());

    private String apiKey;

    @Override
    protected void onBuildConfiguration(ConfigurationBuilder builder) {
        super.onBuildConfiguration(builder);
        builder.setAllowBatchTranslationBySeparator(true);
        builder.setMaxTranslationTextLength(10000);
    }

    @NonNull
    @Override
    public String name() {
        return "{plugin_name}";
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
    public void onStart() {
        apiKey = getContext().getPreferences().getString(
                DeepSeekConstant.DEEPSEEK_API_KEY_PREFERENCE_KEY,
                DeepSeekConstant.DEEPSEEK_API_KEY_DEFAULT
        ).trim();
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException(getContext().getString("{ds_api_key_empty}"));
        }
        //noinspection deprecation
        LocalString localString = getContext().getLanguageNameLocalString();
        sourceLanguage = "auto".equals(sourceLanguage) ? "Auto detect" : localString.get(sourceLanguage, "en");
        targetLanguage = localString.get(targetLanguage, "en");
        return requestTranslation(apiKey, text, sourceLanguage, targetLanguage);
    }

    static String requestTranslation(
            String apiKey,
            String text,
            String sourceLanguage,
            String targetLanguage
    ) throws IOException {
        String requestBody = buildRequestBody(text, sourceLanguage, targetLanguage);
        Request request = new Request.Builder()
                .url(DeepSeekConstant.DEEPSEEK_API_URL)
                .post(RequestBody.create(JSON_MEDIA_TYPE, requestBody))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            ResponseBody body = response.body();
            String bodyText = body == null ? "" : body.string();
            if (!response.isSuccessful()) {
                throw new IOException(parseErrorMessage(response.code(), response.message(), bodyText));
            }
            return parseResult(bodyText);
        }
    }

    private static String parseResult(String bodyText) throws IOException {
        if (bodyText.isEmpty()) {
            throw new IOException("Empty response");
        }
        try {
            JSONObject json = new JSONObject(bodyText);
            JSONArray choices = json.getJSONArray("choices");
            if (choices.isEmpty()) {
                throw new IOException("Parse result failed: choices is empty");
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.getString("content");
            if (content == null || content.isEmpty()) {
                throw new IOException("Parse result failed: content is empty");
            }
            return content;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Parse result failed: " + abbreviate(bodyText), e);
        }
    }

    private static String parseErrorMessage(int code, String message, String bodyText) {
        if (!bodyText.isEmpty()) {
            try {
                JSONObject json = new JSONObject(bodyText);
                if (json.contains("error")) {
                    JSONObject error = json.getJSONObject("error");
                    if (error.contains("message")) {
                        String errorMessage = error.getString("message");
                        if (errorMessage != null && !errorMessage.isEmpty()) {
                            return "HTTP " + code + ": " + errorMessage;
                        }
                    }
                }
            } catch (Exception ignore) {
                // Ignore parse errors and fallback to generic HTTP message.
            }
        }
        if (message != null && !message.isEmpty()) {
            return "HTTP " + code + ": " + message;
        }
        if (!bodyText.isEmpty()) {
            return "HTTP " + code + ": " + abbreviate(bodyText);
        }
        return "HTTP " + code;
    }

    static String buildRequestBody(String text, String sourceLanguage, String targetLanguage) {
        String systemPrompt = "You are a translation engine. "
                + "Source language: " + sourceLanguage + ". "
                + "Target language: " + targetLanguage + ". "
                + "Translate the user text into the target language and output only the translated text. "
                + "Do not add explanations or notes. "
                + "Keep original line breaks and paragraph order. "
                + "Keep punctuation style consistent with the source text. "
                + "If the source text has no ending punctuation, do not add ending punctuation in translation. "
                + "Do not translate placeholders and special tokens such as %s, %1$d, {name}, {0}.";

        JSONArray messages = new JSONArray()
                .add(new JSONObject()
                        .add("role", "system")
                        .add("content", systemPrompt))
                .add(new JSONObject()
                        .add("role", "user")
                        .add("content", text));

        JSONObject request = new JSONObject()
                .add("model", DeepSeekConstant.DEEPSEEK_MODEL)
                .add("temperature", 1.3)
                .add("stream", false)
                .add("messages", messages);
        return request.toString();
    }

    private static String abbreviate(String text) {
        final int maxLength = 180;
        String oneLine = text.replace('\n', ' ').replace('\r', ' ').trim();
        if (oneLine.length() <= maxLength) {
            return oneLine;
        }
        return oneLine.substring(0, maxLength) + "...";
    }
}
