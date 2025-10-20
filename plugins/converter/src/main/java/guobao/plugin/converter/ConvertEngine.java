package guobao.plugin.converter;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.io.IOException;
import java.util.List;

import bin.mt.plugin.api.PluginContext;

import bin.mt.plugin.api.translation.BaseTranslationEngine;

public class ConvertEngine extends BaseTranslationEngine {

    private PluginContext context;

    private final List<String> tools = Arrays.asList("case", "md_ubb", "unicode", "zshh");

    private final List<String> COMMON = Arrays.asList("decode", "encode");
    private final List<String> MD_UBB = Arrays.asList("ubb", "md");
    private final List<String> CASE = Arrays.asList("upper", "lower", "reverse", "constant", "snake", "camel", "kebab", "space"/*, "pascal"*/);

    public ConvertEngine() {
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
        return context.getString("name");
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
            case "md_ubb": return MD_UBB;
            case "zshh":
                context.showToastL(context.getString("warning"));
                return COMMON;
            default:
                return COMMON;
        }
    }

    @NonNull
    @Override
    public String getLanguageDisplayName(String language) {
        return context.getString(language);
    }

    @NonNull
    @Override
    public String translate(String text, String tool, String to) throws IOException {
        Converter cvt = new Converter(context);
        return cvt.convert(text, tool, to);
    }
}