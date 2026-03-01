package guobao.plugin.translator.deeplx;

import android.content.SharedPreferences;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        "zh", "zh-CN", "zh-TW", "en-US", "en-GB", "ja", "ru", "ko", "de", "fr", "es", "it", "pt-BR", "pt-PT",
        "nl", "pl", "ar", "tr", "id", "vi", "th", "sv", "da", "fi", "el", "cs",
        "hu", "ro", "nb", "uk", "bg", "sk", "sl", "lt", "lv", "et", "he", "hi"
    );

    private PluginContext context;

    @Override
    protected void init() {
        context = getContext();
    }

    protected void onBuildConfiguration(ConfigurationBuilder builder) {
        super.onBuildConfiguration(builder);
        builder.setAllowBatchTranslationBySeparator(true);
        builder.setMaxTranslationTextLength(10000);
        //builder.setTargetLanguageMutable(true);
    }

    @Override
    public String name() {
        return "{interface_name_web}";
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
            context.log(result.toString());
            return result.text();
        } catch (DeepLWebTranslator.DeepLException e) {
            context.log(e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void onFinish() {}
}
