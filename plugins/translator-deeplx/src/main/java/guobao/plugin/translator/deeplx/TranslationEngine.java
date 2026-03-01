package guobao.plugin.translator.deeplx;

import android.content.SharedPreferences;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

public class TranslationEngine extends BaseTranslationEngine {
    // 源语言列表（按使用量排序）
    private final List<String> sourceLanguages = Arrays.asList(
        "auto", "en", "zh-TW", "zh-CN", "zh", "ja", "ru", "ko", "de", "fr", "es", "it", "pt",
        "nl", "pl", "ar", "tr", "id", "vi", "th", "sv", "da", "fi", "el", "cs", "hu",
        "ro", "no", "uk", "bg", "sk", "sl", "lt", "lv", "et", "he", "hi"
    );

    private final List<String> targetLanguages = Arrays.asList(
        "zh-CN", "zh-TW", "en-US", "en-GB", "ja", "ru", "ko", "de", "fr", "es", "it", "pt-BR", "pt-PT",
        "nl", "pl", "ar", "tr", "id", "vi", "th", "sv", "da", "fi", "el", "cs",
        "hu", "ro", "nb", "uk", "bg", "sk", "sl", "lt", "lv", "et", "he", "hi"
    );

    private PluginContext context;
    private OkHttpClient httpClient;

    @Override
    protected void init() {
        context = getContext();
        httpClient = new OkHttpClient.Builder().build();
    }

    protected void onBuildConfiguration(ConfigurationBuilder builder) {
        /*super.onBuildConfiguration(builder);
        builder.setAllowBatchTranslationBySeparator(true);
        builder.setMaxTranslationTextLength(4000);
        builder.setTargetLanguageMutable(true);*/
    }

    @Override
    public String name() {
        return "{name}";
    }

    @Override
    public List<String> loadSourceLanguages() {
        return sourceLanguages;
    }

    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return targetLanguages;
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        try (var translator = new DeepLWebTranslator(context)) {
            DeepLWebTranslator.TranslationResult result =
                    translator.translate(text, sourceLanguage, targetLanguage);
            context.log(result.text());
            return result.text();
        } catch (DeepLWebTranslator.DeepLException e) {
            context.log(e.getMessage());
            throw new IOException(e.getMessage(), e);
        }
        /*DeepLXTranslator DeepLX = new DeepLXTranslator(context);
        return DeepLX.translate(text, sourceLanguage, targetLanguage);*/
    }

    @Override
    public void onFinish() {}
}
