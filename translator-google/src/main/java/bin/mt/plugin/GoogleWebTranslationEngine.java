package bin.mt.plugin;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.translation.BaseBatchTranslationEngine;
import bin.mt.plugin.api.translation.BatchTranslationEngine;

/**
 * 谷歌翻译Web版
 *
 * @author Bin
 */
public class GoogleWebTranslationEngine extends BaseBatchTranslationEngine {
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

    private final List<String> targetLanguages = Arrays.asList(
            "zh", "zh-TW", "en", "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca",
            "ceb", "co", "cs", "cy", "da", "de", "el", "eo", "es", "et", "eu", "fa",
            "fi", "fr", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "hi", "hmn", "hr",
            "ht", "hu", "hy", "id", "ig", "is", "it", "iw", "ja", "jw", "ka", "kk",
            "km", "kn", "ko", "ku", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi",
            "mk", "ml", "mn", "mr", "ms", "mt", "my", "ne", "nl", "no", "ny", "pa",
            "pl", "ps", "pt", "ro", "ru", "sd", "si", "sk", "sl", "sm", "sn", "so",
            "sq", "sr", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr",
            "ug", "uk", "ur", "uz", "vi", "xh", "yi", "yo", "zu");

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
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        return GoogleWebTranslator.translate(text, sourceLanguage, targetLanguage);
    }

    @Override
    public BatchTranslationEngine.BatchingStrategy createBatchingStrategy() {
        // 实际限制5000，留点余量
        return new BatchTranslationEngine.DefaultBatchingStrategy(100, 4500) {
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
