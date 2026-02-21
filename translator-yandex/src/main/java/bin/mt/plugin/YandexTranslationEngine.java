package bin.mt.plugin;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.translation.BaseBatchTranslationEngine;

/**
 * Yandex翻译API版
 *
 * @author Bin
 */
public class YandexTranslationEngine extends BaseBatchTranslationEngine {
    private final List<String> sourceLanguages = Arrays.asList("auto",
            "zh", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "ceb", "cs", "cy",
            "da", "de", "el", "eo", "es", "et", "eu", "fa", "fi", "fr", "ga", "gd", "gl", "gu",
            "he", "hi", "hr", "ht", "hu", "hy", "id", "is", "it", "ja", "jv", "ka", "kk", "km",
            "kn", "ko", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mr",
            "ms", "mt", "my", "ne", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si", "sk", "sl",
            "sq", "sr", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr", "tt", "uk", "ur",
            "uz", "vi", "xh", "yi");

    private final List<String> targetLanguages = Arrays.asList(
            "zh", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "ceb", "cs", "cy",
            "da", "de", "el", "eo", "es", "et", "eu", "fa", "fi", "fr", "ga", "gd", "gl", "gu",
            "he", "hi", "hr", "ht", "hu", "hy", "id", "is", "it", "ja", "jv", "ka", "kk", "km",
            "kn", "ko", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mr",
            "ms", "mt", "my", "ne", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si", "sk", "sl",
            "sq", "sr", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr", "tt", "uk", "ur",
            "uz", "vi", "xh", "yi");

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

    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        if (language.equals("he"))
            language = "iw";
        else if (language.equals("jv"))
            language = "jw";
        return super.getLanguageDisplayName(language);
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        if (text == null || text.length() <= 10000) {
            return YandexWebTranslator.translate(text, sourceLanguage, targetLanguage);
        } else {
            List<String> textChunks = splitText(text);

            StringBuilder translatedText = new StringBuilder();
            for (String chunk : textChunks) {
                String translatedChunk = YandexWebTranslator.translate(chunk, sourceLanguage, targetLanguage);
                translatedText.append(translatedChunk);
            }

            return translatedText.toString();
        }
    }

    private static final char[] SPLIT_DELIMITERS = {'.', '?', '!', '？', '！', ',', ' '};

    private static List<String> splitText(String input) {
        List<String> result = new ArrayList<>();
        int maxLength = 10000;

        while (input.length() > maxLength) {
            int endIndex = -1;
            for (char delimiter : SPLIT_DELIMITERS) {
                endIndex = input.lastIndexOf(delimiter, maxLength - 1);
                if (endIndex != -1) break;
            }

            if (endIndex == -1) {
                result.add(input.substring(0, maxLength));
                input = input.substring(maxLength);
            } else {
                result.add(input.substring(0, endIndex + 1));
                input = input.substring(endIndex + 1);
            }
        }

        if (!input.isEmpty()) {
            result.add(input);
        }

        return result;
    }

    @Override
    public BatchingStrategy createBatchingStrategy() {
        // 实际限制10000，留点余量
        return new DefaultBatchingStrategy(100, 9500) {
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

}
