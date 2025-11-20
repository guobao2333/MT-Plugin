package guobao.plugin.converter;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;

public class CaseTest {

    @Test
    public void testDefaultCamelAndSnake() {
        String s = "ReentrantReadWriteLock";
        String snake = Case.toSnakeCase(s, Case.TokenizerConfig.defaults(), false);
        Assert.assertEquals("reentrant_read_write_lock", snake);

        String camel = Case.toCamelCase("myHTTPServer", Case.TokenizerConfig.defaults(), false, true);
        Assert.assertEquals("myHTTPServer", camel); // 保留 HTTP 缩写（默认 preserveAcronyms true）
    }

    @Test
    public void testJavaImportPreserveCaseAfterDot() {
        Case.TokenizerConfig cfg = new Case.TokenizerConfig.Builder()
                .delimiters(".")
                .protectedRules(Case.SplitRule.CASE) // 直接传 SplitRule，更简洁
                .build();

        String in = "java.util.concurrent.locks.ReentrantReadWriteLock";
        String out = Case.toOriginalTokens(in, cfg, ".");
        Assert.assertEquals("java.util.concurrent.locks.ReentrantReadWriteLock", out);
    }

    @Test
    public void testMultiCharDelimiterAndProtectNum() {
        Case.TokenizerConfig cfg = new Case.TokenizerConfig.Builder()
                .delimiters("::")
                .protectedRules(Case.SplitRule.CASE, Case.SplitRule.NUMBER)
                .build();

        String in = "path::ToHTTP2Server::more";
        String out = Case.toOriginalTokens(in, cfg, "::");
        Assert.assertEquals("path::ToHTTP2Server::more", out);
    }

    @Test
    public void testNumberBoundary() {
        Case.TokenizerConfig cfg = Case.TokenizerConfig.defaults();
        String in = "foo2Bar";
        String snake = Case.toSnakeCase(in, cfg, false);
        Assert.assertEquals("foo_2_bar", snake);
    }

    @Test
    public void testDelimitersLongestMatch() {
        Case.TokenizerConfig cfg = new Case.TokenizerConfig.Builder()
                .delimiters(".", "::", ":::")
                .protectedRules(Case.SplitRule.CASE)
                .build();
        // ensure ::: is matched as one delimiter (longest)
        String in = "a:::HTTPServer";
        String out = Case.toOriginalTokens(in, cfg, ":::");
        Assert.assertEquals("a:::HTTPServer", out);
    }
}
