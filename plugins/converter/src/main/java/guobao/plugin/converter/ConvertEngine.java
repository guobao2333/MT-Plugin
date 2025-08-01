package guobao.plugin.converter;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.io.IOException;
import java.util.List;

import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

public class ConvertEngine extends BaseTranslationEngine {

    private LocalString string;
    private MTPluginContext context;

    private final List<String> tools = Arrays.asList("case", "unicode", "zshh");

    private final List<String> CASE = Arrays.asList("upper", "lower", "snake", "camel");
    private final List<String> COMMON = Arrays.asList("decode", "encode");

    public ConvertEngine() {
        super(new ConfigurationBuilder()
        // 关闭「跳过已翻译词条」
        .setForceNotToSkipTranslated(true)
        // 目标语言可变
        .setTargetLanguageMutable(true)
        .build());
    }

    @Override
    protected void init() {
        context = getContext();
        string = context.getAssetLocalString("String");
    }

    @NonNull
    @Override
    public String name() {
        return string.get("name");
    }

    @NonNull
    @Override
    public List<String> loadSourceLanguages() {
        return tools;
    }

    @NonNull
    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        switch (sourceLanguage) {
            case "case": return CASE;
            case "zshh":
                context.showToastL(string.get("warning"));
            default:
                return COMMON;
        }
    }

    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        return string.get(language);
    }

    @NonNull
    @Override
    public String translate(String text, String tool, String to) throws IOException {
        Converter cvt = new Converter(context);
        return cvt.convert(text, tool, to);
    }
}