package guobao.plugin.translator.ai;

import androidx.annotation.NonNull;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

public class TranslationEngine extends BaseTranslationEngine {

    private PluginContext context;

private final List<String> sourceLanguages = Arrays.asList("deepseek","chatgpt","gemini");

    private final List<String> targetLanguages = Arrays.asList(
            "zh-cn","zh-tw","en","zh","ru","ja","en-gb","en","ar","bg","cs","da",
            "de","el","es","et","fi","fr","hu","id","it","ko","lt","lv","nb","nl","pl",
            "pt","ro","sk","sl","sv","tr","uk");

    public TranslationEngine() {
        super(new ConfigurationBuilder()
        // 关闭「跳过已翻译词条」
        .setForceNotToSkipTranslated(true)
        // 目标语言可变
        .setTargetLanguageMutable(true)
        .build());
    }

    @NonNull
    @Override
    protected void init() {
        context = getContext();
    }

    @NonNull
    @Override
    public String name() {
        return "{plugin_name}";
    }

    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        List<String> aiList = new java.util.ArrayList<>();
        for (String list : sourceLanguages) {
            aiList.add(context.getString(list));
        }
        return aiList;
    }

    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return targetLanguages;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        // GeminiTranslator gemini = new GeminiTranslator(context);
        // return gemini.translate(text, sourceLanguage, targetLanguage);
        String logs = "原语言：" + sourceLanguage + "\n目标语言：" + targetLanguage;
        context.log(logs);
        return logs;
    }
}
