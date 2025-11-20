@file:JvmName("StringCompat")
package guobao.plugin.converter.util

import java.util.regex.Matcher
import java.util.regex.MatchResult
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.math.max

/**
 * Kotlin 扩展：补全老 Android 上可能缺失的 String 方法（与 JDK 行为保持一致）。
 * 使用 "Compat" 前缀避免与系统方法冲突。
 *
 * Kotlin 调用：
 *   "a1b2".compatReplaceAll("\\d+", "_")
 *   " a ".compatStrip()        // -> "a"
 *   "a".compatRepeat(3)        // -> "aaa"
 *
 * Java 调用：
 *   StringCompat.replaceAll("a1b2", "\\d+", "_");
 */

// 正则替换（行为与 JDK 一致）

/**
 * 等价于 java.lang.String.replaceAll(regex, replacement)
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatReplaceAll(regex: String, replacement: String): String {
    val p = Pattern.compile(regex)
    val m = p.matcher(this)
    return m.replaceAll(replacement)
}

/**
 * 等价于 java.lang.String.replaceAll(regex, function) - JDK 9+ 行为
 * 使用函数动态生成替换内容
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatReplaceAll(regex: String, replacement: (java.util.regex.MatchResult) -> String): String {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(this)
    val sb = StringBuffer() // 使用 StringBuffer 因为 appendReplacement 需要它
    
    while (matcher.find()) {
        val matchResult = matcher.toMatchResult()
        val replacementStr = replacement(matchResult)
        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacementStr))
    }
    matcher.appendTail(sb)
    return sb.toString()
}

/**
 * 使用 Pattern 对象
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatReplaceAll(pattern: Pattern, replacement: (java.util.regex.MatchResult) -> String): String {
    val matcher = pattern.matcher(this)
    val sb = StringBuffer()
    
    while (matcher.find()) {
        val matchResult = matcher.toMatchResult()
        val replacementStr = replacement(matchResult)
        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacementStr))
    }
    matcher.appendTail(sb)
    return sb.toString()
}

/**
 * 等价于 java.lang.String.replaceFirst(regex, replacement)
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatReplaceFirst(regex: String, replacement: String): String {
    val p = Pattern.compile(regex)
    val m = p.matcher(this)
    return m.replaceFirst(replacement)
}

/**
 * 等价于 java.lang.String.matches(regex)
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatMatches(regex: String): Boolean {
    return Pattern.matches(regex, this)
}

/**
 * 等价于 String.split(regex) / split(regex, limit)
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatSplit(regex: String, limit: Int = 0): Array<String> {
    val p = Pattern.compile(regex)
    return p.split(this, limit)
}

/**
 * 判断是否为空白（全部为 Unicode 空白字符）—— 与 JDK String.isBlank 行为一致
 */
fun String.compatIsBlank(): Boolean {
    if (this.isEmpty()) return true
    for (ch in this) {
        if (!ch.isWhitespace()) return false
    }
    return true
}

/**
 * strip(): 去除两端 Unicode 空白
 */
fun String.compatStrip(): String {
    return this.compatStripLeading().compatStripTrailing()
}

/**
 * stripLeading()
 */
fun String.compatStripLeading(): String {
    var i = 0
    val len = this.length
    while (i < len && this[i].isWhitespace()) i++
    return if (i == 0) this else this.substring(i, len)
}

/**
 * stripTrailing()
 */
fun String.compatStripTrailing(): String {
    var end = this.length
    while (end > 0 && this[end - 1].isWhitespace()) end--
    return if (end == this.length) this else this.substring(0, end)
}

/**
 * repeat(n)：重复字符串 n 次（JDK 11 的 repeat）
 * - n < 0 抛 IllegalArgumentException
 */
fun String.compatRepeat(count: Int): String {
    if (count < 0) throw IllegalArgumentException("count is negative: $count")
    if (count == 0) return ""
    if (this.isEmpty()) return ""
    val sb = StringBuilder(this.length * count)
    repeat(count) { sb.append(this) }
    return sb.toString()
}

/**
 * lines()：按 JDK 行语义（分割 \r\n、\r、\n）
 */
fun String.compatLines(): List<String> {
    val res = ArrayList<String>()
    var i = 0
    val len = this.length
    var start = 0
    while (i < len) {
        val c = this[i]
        if (c == '\r') {
            res.add(this.substring(start, i))
            i++
            if (i < len && this[i] == '\n') i++
            start = i
        } else if (c == '\n') {
            res.add(this.substring(start, i))
            i++
            start = i
        } else {
            i++
        }
    }
    // tail
    if (start <= len) {
        res.add(this.substring(start, len))
    }
    return res
}

/**
 * 当 replacement 中包含 $ 等特殊字符时，使用字面文本替换
 */
@Throws(NullPointerException::class, PatternSyntaxException::class)
fun String.compatReplaceAllLiteral(regex: String, replacementLiteral: String): String {
    val p = Pattern.compile(regex)
    val m = p.matcher(this)
    val safe = Matcher.quoteReplacement(replacementLiteral)
    return m.replaceAll(safe)
}

/**
 * Java 调用函数（静态包装）
 * Java 调用示例：
 *   StringCompat.replaceAll("a1b2", "\\d+", "_");
 */

@JvmName("replaceAllCompat")
fun replaceAllCompat(s: String, regex: String, replacement: String): String = s.compatReplaceAll(regex, replacement)

@JvmName("replaceAllCompat")
fun replaceAllCompat(s: String, regex: String, replacement: java.util.function.Function<java.util.regex.MatchResult, String>): String =
    s.compatReplaceAll(regex) { matchResult -> replacement.apply(matchResult) }

@JvmName("replaceAllCompat")
fun replaceAllCompat(s: String, pattern: Pattern, replacement: java.util.function.Function<java.util.regex.MatchResult, String>): String =
    s.compatReplaceAll(pattern) { matchResult -> replacement.apply(matchResult) }

@JvmName("replaceFirstCompat")
fun replaceFirstCompat(s: String, regex: String, replacement: String): String = s.compatReplaceFirst(regex, replacement)

@JvmName("matchesCompat")
fun matchesCompat(s: String, regex: String): Boolean = s.compatMatches(regex)

@JvmName("splitCompat")
fun splitCompat(s: String, regex: String, limit: Int = 0): Array<String> = s.compatSplit(regex, limit)

@JvmName("isBlankCompat")
fun isBlankCompat(s: String): Boolean = s.compatIsBlank()

@JvmName("stripCompat")
fun stripCompat(s: String): String = s.compatStrip()

@JvmName("repeatCompat")
fun repeatCompat(s: String, count: Int): String = s.compatRepeat(count)

@JvmName("linesCompat")
fun linesCompat(s: String): List<String> = s.compatLines()

@JvmName("replaceAllLiteralReplacementCompat")
fun replaceAllLiteralReplacementCompat(s: String, regex: String, replacementLiteral: String): String = 
    s.compatReplaceAllLiteral(regex, replacementLiteral)