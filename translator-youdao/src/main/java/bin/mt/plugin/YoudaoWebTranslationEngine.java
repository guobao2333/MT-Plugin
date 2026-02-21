package bin.mt.plugin;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bin.mt.plugin.api.translation.BaseBatchTranslationEngine;

/**
 * 有道翻译Web版
 *
 * @author Bin
 */
public class YoudaoWebTranslationEngine extends BaseBatchTranslationEngine {
    private final List<String> sourceLanguages = Arrays.asList("zh", "en", "ja", "fr", "de", "ru",
            "es", "pt", "it", "vi", "id", "ar", "nl", "th");
    private final List<String> targetLanguages1 = sourceLanguages.subList(1, sourceLanguages.size());
    private final List<String> targetLanguages2 = Collections.singletonList("zh");

    public YoudaoWebTranslationEngine() {
        super(new ConfigurationBuilder()
                .setAutoRepairFormatSpecifiersError(true)
                .setTargetLanguageMutable(true)
                .build()
        );
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
        if (sourceLanguage.equals("zh")) {
            return targetLanguages1;
        }
        return targetLanguages2;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        return YoudaoWebTranslator.translate(text, sourceLanguage, targetLanguage);
    }

    @NonNull
    @Override
    public String[] batchTranslate(String[] texts, String sourceLanguage, String targetLanguage) throws IOException {
        return batchTranslateBySingleTranslate(texts, sourceLanguage, targetLanguage);
    }
}
