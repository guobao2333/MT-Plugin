package guobao.plugin.converter;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConvertEngine extends BaseTranslationEngine {

    private LocalString string;
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
        this.string = string;
    }

    public void init() {
        this.context = getContext();
    }

    public String name() {
        return context.getString("name");
    }

    public List<String> loadSourceLanguages() {
        return tools;
    }

    public List<String> loadTargetLanguages(String sourceLanguage) {
        switch (sourceLanguage) {
            case "case": return CASE;
            case "md_ubb": return MD_UBB;
            case "zshh":
                context.showToastL(string.get("warning"));
                return COMMON;
            default:
                return COMMON;
        }
    }
    
    public String getLanguageDisplayName(String language) {
        return context.getString(language);
    }

    public String translate(String text, String tool, String to) throws IOException {
        Converter cvt = new Converter(context);
        return cvt.convert(text, tool, to);
    }
}