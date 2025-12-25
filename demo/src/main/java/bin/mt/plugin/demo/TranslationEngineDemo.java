package bin.mt.plugin.demo;

import androidx.annotation.NonNull;

import java.util.List;

import bin.mt.plugin.api.translation.BaseTranslationEngine;

public class TranslationEngineDemo extends BaseTranslationEngine {

    public TranslationEngineDemo() {
        super(new ConfigurationBuilder()
                // 关闭「跳过已翻译词条」
                .setForceNotToSkipTranslated(true)
                .build());
    }

    /**
     * 翻译引擎名称
     */
    @NonNull
    @Override
    public String name() {
        return "{case_conversion}";
    }

    /**
     * 源语言代码列表
     */
    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return List.of("src");
    }

    /**
     * 目标语言代码列表
     */
    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return List.of("upper", "lower");
    }

    /**
     * 将语言代码转为可视化名称
     */
    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        return switch (language) {
            case "src" -> "原文";
            case "upper" -> "大写";
            case "lower" -> "小写";
            default -> "???";
        };
    }

    /**
     * MT在翻译每一个词条时都会调用一次这个方法
     *
     * @param text           待翻译内容
     * @param sourceLanguage 源语言代码
     * @param targetLanguage 目标语言代码
     * @return 翻译结果
     */
    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (targetLanguage.equals("upper"))
            return text.toUpperCase(); // 转为大写
        else
            return text.toLowerCase(); // 转为小写
    }
}