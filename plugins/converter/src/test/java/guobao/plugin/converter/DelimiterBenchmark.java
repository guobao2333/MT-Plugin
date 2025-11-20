package guobao.plugin.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 简单的 micro-benchmark：对比 Trie 的 longestMatch 与 朴素 list 遍历匹配性能。
 *
 * 说明：基准非常轻量，仅用于粗略比较相对差异（不同 JVM / CPU 会有差异）。
 */
public class DelimiterBenchmark {

    private static String naiveLongestMatch(char[] buf, int pos, int len, List<String> delims) {
        String best = null;
        for (String d : delims) {
            if (d == null || d.length() == 0) continue;
            int dl = d.length();
            if (pos + dl > len) continue;
            boolean ok = true;
            for (int k = 0; k < dl; k++) {
                if (buf[pos + k] != d.charAt(k)) { ok = false; break; }
            }
            if (ok) best = d; // 因为 caller 会按长度降序传入，第一次命中就是最长，也可继续检查
        }
        return best;
    }

    public static void main(String[] args) {
        // 构造示例分隔符集（包含短与长项），Builder.build() 会做去重+排序
        List<String> delims = Arrays.asList(":::", "::", "::?", ".", "###", "=>", "->", "<=", "||", "+++",
                "LONG_DELIM_EXAMPLE_1", "LONG_DELIM_EXAMPLE_2", "x", "y", "z");
        Case.TokenizerConfig cfg = new Case.TokenizerConfig.Builder().delimiters(delims).build();

        // 测试字符串：随机组合，包含一些分隔符位置
        String test = "hello:::world::xLONG_DELIM_EXAMPLE_1::some->end###foo";
        char[] buf = test.toCharArray();

        int runs = 5_000_000; // 5M 次调用以放大差异
        long t0, t1;

        // Warmup
        for (int i = 0; i < 20000; i++) {
            cfg.getTrieForTest().longestMatch(buf, i % buf.length, buf.length);
            naiveLongestMatch(buf, i % buf.length, buf.length, cfg.extraDelimiters == null ? new ArrayList<String>() : cfg.extraDelimiters);
        }

        // Trie 测试
        t0 = System.nanoTime();
        String r = null;
        for (int i = 0; i < runs; i++) {
            r = cfg.getTrieForTest().longestMatch(buf, i % buf.length, buf.length);
        }
        t1 = System.nanoTime();
        System.out.println("Trie time ms: " + ((t1 - t0) / 1_000_000) + " lastMatch=" + r);

        // Naive 测试
        t0 = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            r = naiveLongestMatch(buf, i % buf.length, buf.length, cfg.extraDelimiters == null ? new ArrayList<String>() : cfg.extraDelimiters);
        }
        t1 = System.nanoTime();
        System.out.println("Naive time ms: " + ((t1 - t0) / 1_000_000) + " lastMatch=" + r);
    }
}