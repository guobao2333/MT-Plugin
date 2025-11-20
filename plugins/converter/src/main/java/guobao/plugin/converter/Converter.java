package guobao.plugin.converter;

import android.content.SharedPreferences;

import bin.mt.plugin.api.regex.*;
import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

import guobao.plugin.converter.Case;
import guobao.plugin.converter.util.*;

import io.github.guobao2333.bbcoeter.*;
import io.github.guobao2333.bbcoeter.dom.DOMAdapter;
import io.github.guobao2333.bbcoeter.dom.JsoupDOMAdapter;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Converter {

    private PluginContext context;
    private SharedPreferences config;

    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    public Converter(PluginContext context) {
        this.context = context;
        this.config = context.getPreferences();
    }

    /*public void main(String[] args) {
        // context.showToast("开始转换……");
        // 未来的测试代码
    }*/

    public String convert(String t, String tool, String to) throws IOException {
        switch (tool) {
            case "md_ubb": return bbcode(t, to);
            case "case": return strCase(t, to);
            case "unicode": return unicode(t, to);
            case "zshh": return zshh(t, to);
        }
        return "ERROR: 功能开发中";
    }

    public String bbcode(String str, String to) throws IOException {
        try {
        MarkdownUbbConverter cvt = new MarkdownUbbConverter();
        return "ubb".equals(to) ? cvt.toUBB(str) : cvt.toMarkdown(str);

        /*DOMAdapter adapter = new JsoupDOMAdapter();
        BBCodeConverter bbcc = new BBCodeConverter(adapter);

        // 配置
        bbcc.setAllowBBCode(true);
        bbcc.setAllowHTML(true);
        bbcc.setEscapeHtmlInOutput(true);

        return switch(to) {
            case "ubb" -> bbcc.htmlToBBCode(str);
            case "html" -> bbcc.bbcodeToHtml(str);
            case "markdown" -> str; //bbcc.bbcodeToMarkdown(str);
            default -> str;
        };*/

        // return "Development work is still in progress...";
        } catch(Exception err) {
        	throw err;
        }
    }

    public static String unicode(String str, String to) {
        if (str == null || str.isEmpty()) return str;

        return switch (to) {
            case "encode" -> str.chars()
                .mapToObj(c -> c < 128 ? String.valueOf((char) c) : String.format("\\u%04x", c))
                .collect(Collectors.joining());
            case "decode" -> 
                // 如果有报错不用管，因为从kt调用
                StringCompat.replaceAllCompat(str, UNICODE_PATTERN, matchResult -> {
                    String hex = matchResult.group(1);
                    int codePoint = Integer.parseInt(hex, 16);
                    return new String(Character.toChars(codePoint));
                });
            default -> str;
        };
    }

    public String zshh(String source, String target) throws IOException {
        ZshHist zsh = new ZshHist();
        String outputPath = source + "_" + target;
        try {
            zsh.process(source, outputPath, "encode".equals(target));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(context.getString(e.getMessage()));
        }

        return context.getString(target) + context.getString("zshh_out") + outputPath;
    }

    public String strCase(String str, String to) throws IOException {
        if (str == null || str.isEmpty()) return str;

        final boolean upperContinuous = config.getBoolean("upper_continuous", false); // 保持大写
        final boolean splitNumber = config.getBoolean("split_number", false); // 分割数字
        final boolean camelUpper = config.getBoolean("camel_upper", false);
        //Case.TokenizerConfig defaultConfig = new Case.TokenizerConfig(upperContinuous, splitNumber);

        // 使用新的 Builder 模式创建配置
        Case.TokenizerConfig.Builder configBuilder = new Case.TokenizerConfig.Builder()
                .splitUpperContinuous(upperContinuous)
                .rules(Case.SplitRule.CASE, splitNumber ? Case.SplitRule.NUMBER : null);

        Case.TokenizerConfig defaultConfig = configBuilder.build();

        switch (to) {
            case "upper": return str.toUpperCase();
            case "lower": return str.toLowerCase();
            case "constant": return Case.toSnakeCase(str, defaultConfig, true);
            case "reverse": return new StringBuilder(str).reverse().toString();
            case "snake": return Case.toSnakeCase(str, defaultConfig, false);
            case "camel": return Case.toCamelCase(str, defaultConfig, camelUpper, true);
            case "pascal": return Case.toPascalCase(str); // 使用新的快捷方法
            case "path": return Case.toPathCase(str);
            case "kebab": return Case.toKebabCase(str);
            case "chain": return Case.toChainCase(str);
            case "space": return Case.toSpaceCase(str);
            default: return "正在开发中……";
        }
    }

}
