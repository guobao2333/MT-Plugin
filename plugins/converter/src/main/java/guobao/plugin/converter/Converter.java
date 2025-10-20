package guobao.plugin.converter;

import android.content.SharedPreferences;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

import guobao.plugin.converter.util.*;


public class Converter {

    private LocalString string;
    private PluginContext context;
    private SharedPreferences config;

    public Converter(PluginContext context) {
        this.context = context;
        this.config = context.getPreferences();
        //this.string = string;
    }

    public void main(String[] args) {
        // context.showToast("开始转换……");
        // 未来的测试代码
    }

    public String convert(String t, String tool, String to) throws IOException {
        switch (tool) {
            case "md_ubb": return md_ubb(t, to);
            case "case": return strCase(t, to);
            case "unicode": return unicode(t, to);
            case "zshh": return zshh(t, to);
        }
        return "ERROR: 功能开发中";
    }

    public String md_ubb(String str, String to) throws IOException {
        MarkdownUbbConverter cvt = new MarkdownUbbConverter();
        // if ("ubb".equals(to)) {
            // return cvt.toUBB(str);
        // } else {
            // return cvt.toMarkdown(str);
        // }
        return "ubb".equals(to) ? cvt.toUBB(str) : cvt.toMarkdown(str);
        // return "Development work is still in progress...";
    }

    public static String unicode(String str, String to) {
        if (str.isEmpty()) return str;

        if ("encode".equals(to)) {
            // 字符转unicode
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c < 128) {
                    // 保留ASCII
                    sb.append(c);
                } else {
                    sb.append(String.format("\\u%04X", (int) c));
                }
            }
            return sb.toString();
        } else {
            // unicode转字符
            Pattern p = Pattern.compile("\\\\u([0-9A-Fa-f]{1,4})");
            Matcher m = p.matcher(str);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String hex = m.group(1);
                int codePoint = Integer.parseInt(hex, 16);
                m.appendReplacement(sb, new String(Character.toChars(codePoint)));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    public String zshh(String source, String target) throws IOException {
        ZshHist zsh = new ZshHist();
        String outputPath = source + "_" + target;
        try {
            zsh.process(source, outputPath, "encode".equals(target));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(string.get(e.getMessage()));
        }

        return string.get(target) + string.get("zshh_out") + outputPath;
    }

    public String strCase(String str, String to) throws IOException {
        if (str == null || str.isEmpty()) return str;

        final boolean snakeUpperContinuous = config.getBoolean("snake_upper_continuous", false); // 转为蛇形是否保持大写（比如HTTPRequest）
        final boolean snakeNumber = config.getBoolean("snake_number", false);
        final boolean camelUpper = config.getBoolean("camel_upper", false);

        switch (to) {
            case "upper": return str.toUpperCase(); // 大写
            case "lower": return str.toLowerCase(); // 小写
            case "reverse": return new StringBuilder(str).reverse().toString(); // 反转
            case "snake": return toSnakeCase(str, snakeUpperContinuous, snakeNumber, false); // 蛇形
            case "constant": return toSnakeCase(str, snakeUpperContinuous, snakeNumber, true); // 常量
            case "kebab": return toKebabCase(str, snakeUpperContinuous, snakeNumber); // 烤串
            case "space": return toSpaceCase(str, snakeUpperContinuous, snakeNumber); // 单词
            case "camel": return toCamelCase(str, snakeUpperContinuous, snakeNumber, camelUpper, false); // 驼峰
            // case "pascal": return toCamelCase(str, snakeUpperContinuous, snakeNumber, true, true); // 大驼峰
            default: return "正在开发中……";
        }
    }

    // ====== 分词 & helpers ======
    private List<String> tokenizeToWords(String src, boolean splitUpperContinuous, boolean splitNumber) {
        List<String> tokens = new ArrayList<>();
        if (src == null || src.isEmpty()) return tokens;

        String s = src.trim();
        StringBuilder cur = new StringBuilder();
        int len = s.length();

        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                if (cur.length() == 0) {
                    cur.append(ch);
                    continue;
                }
                char prevCh = cur.charAt(cur.length() - 1);
                boolean boundary = false;

                if (splitNumber) {
                    if (Character.isDigit(ch) && !Character.isDigit(prevCh)) boundary = true;
                    if (!Character.isDigit(ch) && Character.isDigit(prevCh)) boundary = true;
                }

                if (!boundary) {
                    if (Character.isUpperCase(ch)) {
                        if (Character.isLowerCase(prevCh)) {
                            boundary = true;
                        } else if (Character.isUpperCase(prevCh)) {
                            if (splitUpperContinuous && (i + 1 < len) && Character.isLowerCase(s.charAt(i + 1))) {
                                boundary = true;
                            }
                        }
                    }
                }

                if (boundary) {
                    tokens.add(cur.toString());
                    cur.setLength(0);
                }
                cur.append(ch);
            } else {
                if (cur.length() > 0) {
                    tokens.add(cur.toString());
                    cur.setLength(0);
                }
            }
        }
        if (cur.length() > 0) tokens.add(cur.toString());
        return tokens;
    }

    private String joinWith(List<String> tokens, String sep, boolean toLower, boolean toUpper) {
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (t == null || t.isEmpty()) continue;
            String w = t;
            if (toLower) w = t.toLowerCase();
            if (toUpper) w = t.toUpperCase();
            if (sb.length() > 0) sb.append(sep);
            sb.append(w);
        }
        return sb.toString();
    }

    private String toSnakeCase(String src, boolean splitUpperContinuous, boolean splitNumber, boolean upper) {
        List<String> tokens = tokenizeToWords(src, splitUpperContinuous, splitNumber);
        if (tokens.isEmpty()) return upper ? src.toUpperCase() : src.toLowerCase();
        return joinWith(tokens, "_", !upper, upper);
    }

    private String toKebabCase(String src, boolean splitUpperContinuous, boolean splitNumber) {
        List<String> tokens = tokenizeToWords(src, splitUpperContinuous, splitNumber);
        return joinWith(tokens, "-", true, false);
    }

    private String toSpaceCase(String src, boolean splitUpperContinuous, boolean splitNumber) {
        List<String> tokens = tokenizeToWords(src, splitUpperContinuous, splitNumber);
        return joinWith(tokens, " ", true, false);
    }

    /**
     * 单个 toCamelCase 实现（保留 preserveAcronyms 参数）
     * @param src 输入
     * @param splitUpperContinuous 是否在连续大写边界处切分 (HTTPServer -> HTTP_Server)
     * @param splitNumber 是否在字母/数字边界处切分 (abc123 -> abc_123)
     * @param camelUpperFirst 是否首字母大写 (PascalCase)
     * @param preserveAcronyms 是否尽量保留全大写的 token（比如 HTTP）
     */
    private String toCamelCase(String src, boolean splitUpperContinuous, boolean splitNumber, boolean camelUpperFirst, boolean preserveAcronyms) {
        List<String> tokens = tokenizeToWords(src, splitUpperContinuous, splitNumber);
        if (tokens.isEmpty()) return src;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token == null || token.isEmpty()) continue;

            // Java7 兼容的 allUpper 判断（替代 token.chars().allMatch(...)）
            boolean allUpper = true;
            for (int k = 0; k < token.length(); k++) {
                char ch2 = token.charAt(k);
                if (Character.isLetter(ch2) && !Character.isUpperCase(ch2)) {
                    allUpper = false;
                    break;
                }
                // 非字母字符（数字等）按照原意视为满足条件（与原 lambda: || !Character.isLetter(c) 一致）
            }

            if (i == 0 && !camelUpperFirst) {
                // lowerCamel: 首 token 小写
                if (preserveAcronyms && allUpper) {
                    sb.append(token.toLowerCase());
                } else {
                    sb.append(Character.toLowerCase(token.charAt(0)));
                    if (token.length() > 1) sb.append(token.substring(1).toLowerCase());
                }
            } else {
                if (allUpper && preserveAcronyms) {
                    sb.append(token); // 保留缩写全大写
                } else {
                    sb.append(Character.toUpperCase(token.charAt(0)));
                    if (token.length() > 1) sb.append(token.substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }
}
