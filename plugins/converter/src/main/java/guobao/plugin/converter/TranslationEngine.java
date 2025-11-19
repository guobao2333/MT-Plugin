package guobao.plugin.converter;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TranslationEngine extends BaseTranslationEngine {

    private PluginContext context;

    private final List<String> tools = Arrays.asList("case", "md_ubb", "unicode", "zshh");

    private final List<String> COMMON = Arrays.asList("decode", "encode");
    private final List<String> MD_UBB = Arrays.asList("ubb", "html", "markdown");
    private final List<String> CASE = Arrays.asList("upper", "lower", "reverse", "constant", "snake", "camel", "kebab", "space", "chain", "path"/*, "pascal"*/);

    public TranslationEngine() {
        super(new ConfigurationBuilder()
        // 关闭「跳过已翻译词条」
        .setForceNotToSkipTranslated(true)
        // 目标语言可变
        .setTargetLanguageMutable(true)
        .build());
    }

    public void init() {
        this.context = getContext();
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