package guobao.plugin.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 命名风格转换
 *
 * 分词阶段只记录 char[] 的区间索引（Token），避免大量 substring。
 */
public final class Case {

    /** token 表示原 char[] 中的 [start, end] */
    private static final class Token {
        final int start;
        final int end;
        Token(int s, int e) { start = s; end = e; }
        int len() { return end - start; }
    }

    /** 可扩展的分词配置 */
    public static final class TokenizerConfig {
        public final boolean splitUpperContinuous;
        public final boolean splitNumber;

        public TokenizerConfig(boolean splitUpperContinuous, boolean splitNumber) {
            this.splitUpperContinuous = splitUpperContinuous;
            this.splitNumber = splitNumber;
        }

        public static TokenizerConfig defaults() {
            return new TokenizerConfig(true, true);
        }
    }

    private enum CaseMode { LOWER, UPPER, ORIGINAL }

    // ===================== 分词 =====================
    private static List<Token> tokenize(char[] buf, int len, TokenizerConfig cfg) {
        List<Token> tokens = new ArrayList<>();
        if (buf == null || len == 0) return tokens;

        int curStart = -1;
        for (int i = 0; i < len; i++) {
            char ch = buf[i];
            boolean isAlnum = Character.isLetterOrDigit(ch);
            if (!isAlnum) {
                if (curStart >= 0) {
                    tokens.add(new Token(curStart, i));
                    curStart = -1;
                }
                continue;
            }
            if (curStart < 0) {
                curStart = i;
                continue;
            }
            // cur 至少有一个字符；决定 buf[i-1] 和 ch 之间的边界
            char prev = buf[i - 1];
            boolean boundary = false;

            if (cfg.splitNumber) {
                if (Character.isDigit(ch) && !Character.isDigit(prev)) boundary = true;
                else if (!Character.isDigit(ch) && Character.isDigit(prev)) boundary = true;
            }

            if (!boundary) {
                if (Character.isUpperCase(ch)) {
                    if (Character.isLowerCase(prev)) {
                        boundary = true;
                    } else if (Character.isUpperCase(prev) && cfg.splitUpperContinuous) {
                        // 如果下一个是小写且允许在连续大写处切分，则切分：
                        // 例如在 "HTTPServer" 遇到 'S' 后面的 'e' 是小写 => 在 S 前切分成 "HTTP" + "Server"
                        if (i + 1 < len && Character.isLowerCase(buf[i + 1])) {
                            boundary = true;
                        }
                    }
                }
            }

            if (boundary) {
                tokens.add(new Token(curStart, i));
                curStart = i;
            }
        }

        if (curStart >= 0) tokens.add(new Token(curStart, len));
        return tokens;
    }

    // ===================== 通用拼接 =====================
    private static String joinFromTokens(char[] buf, List<Token> tokens, String sep, CaseMode mode) {
        if (tokens == null || tokens.isEmpty()) return new String(buf); // 返回原始字符串
        // 预估长度
        int est = Math.max(16, tokens.size() * 4 + (tokens.size() - 1) * (sep == null ? 0 : sep.length()));
        StringBuilder sb = new StringBuilder(est);
        boolean first = true;
        for (Token t : tokens) {
            if (!first && sep != null) sb.append(sep);
            first = false;
            if (mode == CaseMode.ORIGINAL) {
                sb.append(buf, t.start, t.len());
            } else if (mode == CaseMode.LOWER) {
                for (int i = t.start; i < t.end; i++) sb.append(Character.toLowerCase(buf[i]));
            } else { // UPPER
                for (int i = t.start; i < t.end; i++) sb.append(Character.toUpperCase(buf[i]));
            }
        }
        return sb.toString();
    }

    /**
     * 构造 camel/pascal
     *
     * @param buf 源 char[]
     * @param tokens 分词结果
     * @param camelUpperFirst 首字母是否大写（true = PascalCase）
     * @param preserveAcronyms 是否保留全大写 token（例如 "HTTP"）
     */
    private static String camelFromTokens(char[] buf, List<Token> tokens, boolean camelUpperFirst, boolean preserveAcronyms) {
        if (tokens == null || tokens.isEmpty()) return new String(buf);

        // 估算长度
        int est = 0;
        for (Token t : tokens) est += t.len();
        StringBuilder sb = new StringBuilder(Math.max(16, est));

        for (int idx = 0; idx < tokens.size(); idx++) {
            Token t = tokens.get(idx);
            int s = t.start, e = t.end;
            if (s >= e) continue;

            // 判断 token 是否“全大写（忽略非字母）”
            boolean allUpper = true;
            for (int i = s; i < e; i++) {
                char c = buf[i];
                if (Character.isLetter(c) && !Character.isUpperCase(c)) {
                    allUpper = false;
                    break;
                }
            }

            if (idx == 0 && !camelUpperFirst) {
                // lowerCamel: 首 token 首字母小写
                if (preserveAcronyms && allUpper) {
                    // 全大写缩写且要保留时，把整个 token 变成小写（保持可读性：HTTP -> http）
                    for (int i = s; i < e; i++) sb.append(Character.toLowerCase(buf[i]));
                } else {
                    // 首字母小写，剩余小写
                    sb.append(Character.toLowerCase(buf[s]));
                    for (int i = s + 1; i < e; i++) sb.append(Character.toLowerCase(buf[i]));
                }
            } else {
                // 后续 token 首字母大写
                if (allUpper && preserveAcronyms) {
                    // 保留缩写全大写（直接复制原样）
                    for (int i = s; i < e; i++) sb.append(buf[i]);
                } else {
                    sb.append(Character.toUpperCase(buf[s]));
                    for (int i = s + 1; i < e; i++) sb.append(Character.toLowerCase(buf[i]));
                }
            }
        }
        return sb.toString();
    }

    // ===================== 公开方法 =====================

    public static String defaultCase(String separator, String src, TokenizerConfig cfg) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return src.toLowerCase();
        return joinFromTokens(buf, tokens, separator, CaseMode.LOWER);
    }

    public static String toSnakeCase(String src, TokenizerConfig cfg, boolean upper) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return upper ? src.toUpperCase() : src.toLowerCase();
        return joinFromTokens(buf, tokens, "_", upper ? CaseMode.UPPER : CaseMode.LOWER);
    }

    public static String toCamelCase(String src, TokenizerConfig cfg, boolean camelUpperFirst, boolean preserveAcronyms) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return src;
        return camelFromTokens(buf, tokens, camelUpperFirst, preserveAcronyms);
    }

    // ===================== 重载方法 =====================

    public static String toPathCase(String src) { return defaultCase("/", src, TokenizerConfig.defaults()); }
    public static String toKebabCase(String src) { return defaultCase("-", src, TokenizerConfig.defaults()); }
    public static String toChainCase(String src) { return defaultCase(".", src, TokenizerConfig.defaults()); }
    public static String toSpaceCase(String src) { return defaultCase(" ", src, TokenizerConfig.defaults()); }

    public static String toSnakeCase(String src, boolean upper) { return toSnakeCase(src, TokenizerConfig.defaults(), upper); }
    // lowerCamel，保留缩写
    public static String toCamelCase(String src) { return toCamelCase(src, TokenizerConfig.defaults(), false, true); }
    // PascalCase，保留缩写
    public static String toPascalCase(String src) { return toCamelCase(src, TokenizerConfig.defaults(), true, true); }
}
