package guobao.plugin.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 命名风格转换工具类
 *
 * <p>设计要点：
 * <ul>
 *   <li>分词阶段仅记录 char[] 的区间索引（Token），避免 substring 分配。</li>
 *   <li>规则配置使用 RuleSet + Builder 风格，支持链式配置</li>
 *   <li>extraDelimiters 使用 Trie 进行最长匹配以提升性能（构造时对分隔符进行了清理与按长度排序）</li>
 * </ul>
 *
 * <p>示例用法：
 * <pre>{@code
 * // 基本转换
 * String snake = Case.toSnakeCase("myHTTPServer", false); // "my_http_server"
 * String camel = Case.toCamelCase("my_http_server");      // "myHttpServer"
 * 
 * // 自定义配置
 * TokenizerConfig config = new TokenizerConfig.Builder()
 *     .rules(SplitRule.CASE)
 *     .delimiters("::", "->")
 *     .protectedRules(SplitRule.CASE)
 *     .build();
 * String result = Case.toSnakeCase("MyClass::getValue", config, false); // "my_class::get_value"
 * }</pre>
 */
public final class Case {

    /**
     * 表示原始字符数组 buf 中的区间 [start, end)
     */
    private static final class Token {
        final int start;
        final int end;
        Token(int s, int e) { start = s; end = e; }
        int len() { return end - start; }
    }

    /**
     * 单个拆分规则枚举。未来新增规则只需在此新增枚举项即可。
     */
    public enum SplitRule {
        /** 按大小写边界拆分（例如 "myHTTPServer" 切分为 my + HTTP + Server） */
        CASE,
        /** 按数字边界拆分（字母与数字相邻时切分） */
        NUMBER,
        /** 预留：按符号/Unicode 类别等拆分 */
        SYMBOL
    }

    /**
     * 不可变的规则集合，基于 EnumSet 实现，提供工厂方法与常用常量
     */
    public static final class RuleSet {
        private final EnumSet<SplitRule> rules;

        private RuleSet(EnumSet<SplitRule> rules) { this.rules = rules; }

        /** 创建包含指定规则的 RuleSet（可传多个或一个）。 */
        public static RuleSet of(SplitRule... rs) {
            if (rs == null || rs.length == 0) return none();
            EnumSet<SplitRule> set = EnumSet.noneOf(SplitRule.class);
            for (SplitRule r : rs) if (r != null) set.add(r);
            return new RuleSet(set);
        }

        /** 空集合（无规则）。 */
        public static RuleSet none() { return new RuleSet(EnumSet.noneOf(SplitRule.class)); }

        /** 默认集合：CASE + NUMBER */
        public static final RuleSet DEFAULT = RuleSet.of(SplitRule.CASE, SplitRule.NUMBER);

        /** 全部规则（用于测试或特殊情况） */
        public static RuleSet all() { return new RuleSet(EnumSet.allOf(SplitRule.class)); }

        /** 是否包含某规则 */
        public boolean has(SplitRule r) { return rules.contains(r); }

        /** 返回一个新的 RuleSet，等于当前集合去掉指定规则 */
        public RuleSet without(SplitRule r) {
            EnumSet<SplitRule> copy = EnumSet.copyOf(rules);
            copy.remove(r);
            return new RuleSet(copy);
        }

        @Override
        public String toString() { return rules.toString(); }
    }

    /**
     * 分词器配置（不可变），使用 Builder 链式构造
     */
    public static final class TokenizerConfig {
        /** 启用的拆分规则集合（默认 CASE + NUMBER） */
        public final RuleSet rules;
        /**
         * 原始的分隔符列表（用于序列化 / 调试），可能为 null
         * 真正的匹配由内部 trie 完成（在构造时构建）
         */
        public final List<String> extraDelimiters;
        /** 在遇到额外分隔符后需要保护（禁止）的规则集合，例如禁止 CASE 则下一个 segment 不会按大小写拆分 */
        public final RuleSet protectedRules;
        /** 在连续大写处是否切分（如 HTTPServer -> HTTP + Server） */
        public final boolean splitUpperContinuous;

        // 内部：Trie，用于快速匹配 extraDelimiters（可能为 null）
        private final DelimiterTrie trie;
        /**
         * 仅供包内基准测试使用，获取分隔符 Trie
         */
        DelimiterTrie getTrieForTest() { return trie; }

        private TokenizerConfig(Builder b) {
            this.rules = b.rules;
            this.extraDelimiters = (b.extraDelimiters == null || b.extraDelimiters.isEmpty()) ? null : new ArrayList<>(b.extraDelimiters);
            this.protectedRules = b.protectedRules;
            this.splitUpperContinuous = b.splitUpperContinuous;
            this.trie = (this.extraDelimiters == null) ? null : new DelimiterTrie(this.extraDelimiters);
        }

        /** 获取默认配置（等价于旧行为：启用 CASE 与 NUMBER，不启用 extraDelimiters）。 */
        public static TokenizerConfig defaults() { return new Builder().build(); }

        /**
         * Builder：链式构造 TokenizerConfig。
         * 支持直接传入 SplitRule（更简洁）或 RuleSet。
         */
        public static final class Builder {
            private RuleSet rules = RuleSet.DEFAULT;
            private List<String> extraDelimiters = null;
            private RuleSet protectedRules = RuleSet.none();
            private boolean splitUpperContinuous = true;

            /**
             * 使用 RuleSet（完整控制）。
             *
             * @param r 规则集合
             * @return builder 自身
             */
            public Builder rules(RuleSet r) { this.rules = r == null ? RuleSet.none() : r; return this; }

            /**
             * 使用一到多个 SplitRule（简洁写法）。
             *
             * @param first 第一个规则（可以与 rest 组合）
             * @param rest 其余规则（可选）
             * @return builder 自身
             */
            public Builder rules(SplitRule first, SplitRule... rest) {
                if (first == null && (rest == null || rest.length == 0)) { this.rules = RuleSet.none(); }
                else {
                    List<SplitRule> list = new ArrayList<>();
                    if (first != null) list.add(first);
                    if (rest != null) for (SplitRule r : rest) if (r != null) list.add(r);
                    this.rules = RuleSet.of(list.toArray(new SplitRule[list.size()]));
                }
                return this;
            }

            /**
             * 设置多字符分隔符（varargs 版本）。
             *
             * @param ds 分隔符（支持多字符）
             * @return builder 自身
             */
            public Builder delimiters(String... ds) {
                if (ds == null || ds.length == 0) this.extraDelimiters = null;
                else this.extraDelimiters = Arrays.asList(ds);
                return this;
            }

            /**
             * 设置多字符分隔符（Collection 版本）。
             *
             * <p>方法会在 {@link #build()} 时进行校验：去掉 null/空串、去重并按长度降序排序以利于 Trie 的最长匹配策略。</p>
             *
             * @param ds 分隔符集合（保留插入顺序，但最终 build 会按长度降序排序）
             * @return builder 自身
             */
            public Builder delimiters(List<String> ds) { this.extraDelimiters = ds; return this; }

            /**
             * 使用 RuleSet 作为 protectedRules（全量）。
             *
             * @param r 要保护的规则集合
             * @return builder 自身
             */
            public Builder protectedRules(RuleSet r) { this.protectedRules = r == null ? RuleSet.none() : r; return this; }

            /**
             * 使用单个或多个 SplitRule 来指定 protectedRules（更简洁）。
             *
             * @param first 第一个规则
             * @param rest 其余规则
             * @return builder 自身
             */
            public Builder protectedRules(SplitRule first, SplitRule... rest) {
                if (first == null && (rest == null || rest.length == 0)) this.protectedRules = RuleSet.none();
                else {
                    List<SplitRule> list = new ArrayList<>();
                    if (first != null) list.add(first);
                    if (rest != null) for (SplitRule r : rest) if (r != null) list.add(r);
                    this.protectedRules = RuleSet.of(list.toArray(new SplitRule[list.size()]));
                }
                return this;
            }

            /**
             * 是否在连续大写处切分（默认 true）。
             *
             * @param v true 则启用
             * @return builder 自身
             */
            public Builder splitUpperContinuous(boolean v) { this.splitUpperContinuous = v; return this; }

            /**
             * 构造最终的 TokenizerConfig。
             *
             * <p>在此处对 extraDelimiters 进行校验与规范化处理：
             * <ul>
             *   <li>去掉 null 或空串</li>
             *   <li>去重（保留首次出现顺序）</li>
             *   <li>按分隔符长度降序排序（有助于 Trie 最长匹配策略）</li>
             * </ul>
             *
             * @return 不可变的 TokenizerConfig
             */
            public TokenizerConfig build() {
                if (this.extraDelimiters != null) {
                    // 去空、去重（保留插入顺序），然后按长度降序排序
                    Set<String> seen = new LinkedHashSet<>();
                    for (String s : this.extraDelimiters) {
                        if (s == null) continue;
                        String t = s.trim();
                        if (t.isEmpty()) continue;
                        seen.add(t);
                    }
                    List<String> normalized = new ArrayList<>(seen);
                    // 按长度降序排序以优先匹配长分隔符
                    normalized.sort((a, b) -> Integer.compare(b.length(), a.length()));
                    this.extraDelimiters = normalized;
                }
                return new TokenizerConfig(this);
            }
        }
    }

    // 简单的拼接模式枚举
    private enum CaseMode { LOWER, UPPER, ORIGINAL }

    // ===================== Delimiter Trie（同前） =====================

    // 由于基准测试需要，设为包级可见
    /* private */ static final class DelimiterTrie {
        private final Node root = new Node();

        private static final class Node {
            Node[] next = null;
            String value = null;
            void ensure() { if (next == null) next = new Node[256]; }
        }

        DelimiterTrie(List<String> delims) {
            for (String d : delims) {
                if (d == null || d.isEmpty()) continue;
                add(d);
            }
        }

        private void add(String s) {
            Node cur = root;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                int idx = ch & 0xFF;
                if (cur.next == null) cur.next = new Node[256];
                if (cur.next[idx] == null) cur.next[idx] = new Node();
                cur = cur.next[idx];
            }
            cur.value = s;
        }

        String longestMatch(char[] buf, int pos, int len) {
            Node cur = root;
            String best = null;
            int i = pos;
            while (i < len && cur != null) {
                if (cur.next == null) break;
                int idx = buf[i] & 0xFF;
                Node next = cur.next[idx];
                if (next == null) break;
                cur = next;
                if (cur.value != null) best = cur.value;
                i++;
            }
            return best;
        }
    }

    // ===================== 分词实现（同前） =====================

    private static List<Token> tokenize(char[] buf, int len, TokenizerConfig cfg) {
        List<Token> tokens = new ArrayList<>();
        if (buf == null || len == 0) return tokens;
        if (cfg == null) cfg = TokenizerConfig.defaults();

        int curStart = -1;
        boolean curProtectCase = false;
        boolean curProtectNum = false;
        boolean nextProtectCase = false;
        boolean nextProtectNum = false;

        int i = 0;
        while (i < len) {
            String matched = null;
            if (cfg.trie != null) matched = cfg.trie.longestMatch(buf, i, len);

            if (matched != null) {
                if (curStart >= 0) {
                    tokens.add(new Token(curStart, i));
                    curStart = -1;
                    curProtectCase = false;
                    curProtectNum = false;
                }
                nextProtectCase = cfg.protectedRules.has(SplitRule.CASE);
                nextProtectNum  = cfg.protectedRules.has(SplitRule.NUMBER);
                i += matched.length();
                continue;
            }

            char ch = buf[i];
            boolean isAlnum = Character.isLetterOrDigit(ch);

            if (!isAlnum) {
                if (curStart >= 0) {
                    tokens.add(new Token(curStart, i));
                    curStart = -1;
                    curProtectCase = false;
                    curProtectNum = false;
                }
                nextProtectCase = false;
                nextProtectNum = false;
                i++;
                continue;
            }

            if (curStart < 0) {
                curStart = i;
                curProtectCase = nextProtectCase;
                curProtectNum = nextProtectNum;
                nextProtectCase = false;
                nextProtectNum = false;
                i++;
                continue;
            }

            char prev = buf[i - 1];
            boolean boundary = false;

            if (cfg.rules.has(SplitRule.NUMBER) && !curProtectNum) {
                if (Character.isDigit(ch) && !Character.isDigit(prev)) boundary = true;
                else if (!Character.isDigit(ch) && Character.isDigit(prev)) boundary = true;
            }

            if (!boundary && cfg.rules.has(SplitRule.CASE) && !curProtectCase) {
                if (Character.isUpperCase(ch)) {
                    if (Character.isLowerCase(prev)) boundary = true;
                    else if (Character.isUpperCase(prev) && cfg.splitUpperContinuous) {
                        if (i + 1 < len && Character.isLowerCase(buf[i + 1])) boundary = true;
                    }
                }
            }

            if (boundary) {
                tokens.add(new Token(curStart, i));
                curStart = i;
                curProtectCase = false;
                curProtectNum = false;
            }
            i++;
        }

        if (curStart >= 0) tokens.add(new Token(curStart, len));
        return tokens;
    }

    // ===================== 拼接 / 转换（同前） =====================

    private static String joinFromTokens(char[] buf, List<Token> tokens, String sep, CaseMode mode) {
        if (tokens == null || tokens.isEmpty()) return new String(buf);
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
            } else {
                for (int i = t.start; i < t.end; i++) sb.append(Character.toUpperCase(buf[i]));
            }
        }
        return sb.toString();
    }

    private static String camelFromTokens(char[] buf, List<Token> tokens, boolean camelUpperFirst, boolean preserveAcronyms) {
        if (tokens == null || tokens.isEmpty()) return new String(buf);
        int est = 0;
        for (Token t : tokens) est += t.len();
        StringBuilder sb = new StringBuilder(Math.max(16, est));
        for (int idx = 0; idx < tokens.size(); idx++) {
            Token t = tokens.get(idx);
            int s = t.start, e = t.end;
            if (s >= e) continue;
            boolean allUpper = true;
            for (int i = s; i < e; i++) {
                char c = buf[i];
                if (Character.isLetter(c) && !Character.isUpperCase(c)) { allUpper = false; break; }
            }
            if (idx == 0 && !camelUpperFirst) {
                if (preserveAcronyms && allUpper) {
                    for (int i = s; i < e; i++) sb.append(Character.toLowerCase(buf[i]));
                } else {
                    sb.append(Character.toLowerCase(buf[s]));
                    for (int i = s + 1; i < e; i++) sb.append(Character.toLowerCase(buf[i]));
                }
            } else {
                if (allUpper && preserveAcronyms) {
                    for (int i = s; i < e; i++) sb.append(buf[i]);
                } else {
                    sb.append(Character.toUpperCase(buf[s]));
                    for (int i = s + 1; i < e; i++) sb.append(Character.toLowerCase(buf[i]));
                }
            }
        }
        return sb.toString();
    }

    // ===================== 公开 API（向后兼容） =====================

    /**
     * 将输入字符串转换为指定分隔符连接的小写形式。
     *
     * @param separator 分隔符
     * @param src       输入字符串
     * @param cfg       分词器配置
     * @return 转换后的字符串，如果输入为 null 则返回 null
     * 
     * <p>示例：
     * <pre>{@code
     * String result = Case.defaultCase("-", "myHTTPServer", TokenizerConfig.defaults());
     * }</pre>
     */
    public static String defaultCase(String separator, String src, TokenizerConfig cfg) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return src.toLowerCase();
        return joinFromTokens(buf, tokens, separator, CaseMode.LOWER);
    }

    /**
     * 以原始大小写拼接 tokens（适合保留每段原始大小写的场景）。
     *
     * @param src 输入字符串
     * @param cfg 分词器配置
     * @param sep 分隔符
     * @return 转换后的字符串，如果输入为 null 则返回 null
     */
    public static String toOriginalTokens(String src, TokenizerConfig cfg, String sep) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return src;
        return joinFromTokens(buf, tokens, sep, CaseMode.ORIGINAL);
    }

    /**
     * 转换为蛇形命名（snake_case）或大写下划线命名（SCREAMING_SNAKE_CASE）。
     *
     * @param src   输入字符串
     * @param cfg   分词器配置
     * @param upper 是否为大写形式
     * @return 转换后的字符串，如果输入为 null 则返回 null
     * 
     * <p>示例：
     * <pre>{@code
     * String snake = Case.toSnakeCase("myHTTPServer", TokenizerConfig.defaults(), false);
     * // 结果: "my_http_server"
     * 
     * String screaming = Case.toSnakeCase("myHTTPServer", TokenizerConfig.defaults(), true);
     * // 结果: "MY_HTTP_SERVER"
     * }</pre>
     */
    public static String toSnakeCase(String src, TokenizerConfig cfg, boolean upper) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return upper ? src.toUpperCase() : src.toLowerCase();
        return joinFromTokens(buf, tokens, "_", upper ? CaseMode.UPPER : CaseMode.LOWER);
    }

    /**
     * 转换为驼峰命名（camelCase 或 PascalCase）。
     *
     * @param src               输入字符串
     * @param cfg               分词器配置
     * @param camelUpperFirst   是否首字母大写（PascalCase）
     * @param preserveAcronyms  是否保留首字母缩写的大写形式
     * @return 转换后的字符串，如果输入为 null 则返回 null
     */
    public static String toCamelCase(String src, TokenizerConfig cfg, boolean camelUpperFirst, boolean preserveAcronyms) {
        if (src == null) return null;
        char[] buf = src.toCharArray();
        List<Token> tokens = tokenize(buf, buf.length, cfg);
        if (tokens.isEmpty()) return src;
        return camelFromTokens(buf, tokens, camelUpperFirst, preserveAcronyms);
    }

    // ===================== 快捷重载方法 =====================

    /**
     * 转换风格
     *
     * @param src 输入字符串
     * @return 转换后的字符串
     */
    public static String toPathCase(String src) { return defaultCase("/", src, TokenizerConfig.defaults()); }
    public static String toKebabCase(String src) { return defaultCase("-", src, TokenizerConfig.defaults()); }
    public static String toChainCase(String src) { return defaultCase(".", src, TokenizerConfig.defaults()); }
    public static String toSpaceCase(String src) { return defaultCase(" ", src, TokenizerConfig.defaults()); }
    public static String toCamelCase(String src) { return toCamelCase(src, TokenizerConfig.defaults(), false, true); }
    public static String toPascalCase(String src) { return toCamelCase(src, TokenizerConfig.defaults(), true, true); }

    /**
     * 转换为蛇形命名（快捷方法）。
     *
     * @param src   输入字符串
     * @param upper 首字母是否大写
     * @return 转换后的字符串
     */
    public static String toSnakeCase(String src, boolean upper) { return toSnakeCase(src, TokenizerConfig.defaults(), upper); }

    /**
     * 快速构造单一分隔符的配置（向后兼容 helper）。
     *
     * @param delim         分隔符
     * @param protectedRules 保护规则
     * @return TokenizerConfig 配置实例
     * 
     * <p>示例：
     * <pre>{@code
     * TokenizerConfig config = Case.withExtraDelimiter("::", RuleSet.of(SplitRule.CASE));
     * String result = Case.toSnakeCase("MyClass::getValue", config, false); // "my_class::get_value"
     * }</pre>
     */
    public static TokenizerConfig withExtraDelimiter(String delim, RuleSet protectedRules) {
        if (delim == null || delim.isEmpty()) return TokenizerConfig.defaults();
        return new TokenizerConfig.Builder().delimiters(delim).protectedRules(protectedRules).build();
    }
}
