package guobao.plugin.editor.enhancer.format;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * 缩进对齐工具类，用于格式化文本行的对齐和缩进。
 * 支持两种模式：格式化模式（添加逗号）和去除逗号模式。
 * 内容允许存在//注释，智能列宽计算和自定义配置选项。
 * 
 * <p>示例用法：
 * <pre>
 * List<String> lines = Arrays.asList("apple, banana", "cherry");
 * List<String> aligned = IndentationAligner.align(lines, Mode.FORMAT);
 * 
 * // 使用自定义选项
 * AlignmentOptions options = new AlignmentOptions()
 *     .withMinSpacing(3)
 *     .withDelimiter("[,\\s]+");
 * List<String> enhanced = IndentationAligner.align(lines, Mode.FORMAT, options);
 * </pre>
 * 
 * @author  <a href="https://github.com/guobao2333">shiguobaona</a>
 * @since   2025-11-6
 */
public class IndentationAligner {

    /**
     * 对齐模式枚举
     */
    public enum Mode {
        /**
         * 格式化模式：添加逗号，并对齐各列
         */
        FORMAT,

        /**
         * 去除逗号模式：去掉逗号，并对齐各列
         */
        REMOVE_COMMA
    }

    /**
     * 对齐配置选项
     * 
     * <p>示例用法：
     * <pre>
     * AlignmentOptions options = new AlignmentOptions()
     *     .withDelimiter("[,\\s]+")
     *     .withMinSpacing(3)
     *     .withTrimElements(true);
     * </pre>
     */
    public static class AlignmentOptions {
        /** 分隔符正则表达式，默认为逗号和空白字符 */
        public String delimiterRegex = "[,\\s]+";

        /** 注释标识符，默认为"//" */
        public String commentMarker = "//";

        /** 最小列间距，默认为1个空格 */
        public int minColumnSpacing = 1;

        /** 是否保留尾随逗号，默认为false */
        public boolean preserveTrailingComma = false;

        /** 是否修剪元素空白，默认为true */
        public boolean trimElements = true;

        /** 自定义列宽度（覆盖自动计算） */
        public Map<Integer, Integer> customColumnWidths = new HashMap<>();

        /**
         * 默认构造函数
         */
        public AlignmentOptions() {}

        /**
         * 设置分隔符正则表达式
         * 
         * @param delimiter 分隔符正则表达式
         * @return 当前对象，用于链式调用
         */
        public AlignmentOptions withDelimiter(String delimiter) {
            this.delimiterRegex = delimiter;
            return this;
        }

        /**
         * 设置注释标识符
         * 
         * @param commentMarker 注释标识符
         * @return 当前对象，用于链式调用
         */
        public AlignmentOptions withCommentMarker(String commentMarker) {
            this.commentMarker = commentMarker;
            return this;
        }

        /**
         * 设置最小列间距
         * 
         * @param spacing 最小列间距（空格数）
         * @return 当前对象，用于链式调用
         */
        public AlignmentOptions withMinSpacing(int spacing) {
            this.minColumnSpacing = spacing;
            return this;
        }

        /**
         * 设置是否保留尾随逗号
         * 
         * @param preserve 是否保留尾随逗号
         * @return 当前对象，用于链式调用
         */
        public AlignmentOptions withPreserveTrailingComma(boolean preserve) {
            this.preserveTrailingComma = preserve;
            return this;
        }

        /**
         * 设置是否修剪元素空白
         * 
         * @param trim 是否修剪元素空白
         * @return 当前对象，用于链式调用
         */
        public AlignmentOptions withTrimElements(boolean trim) {
            this.trimElements = trim;
            return this;
        }

        /**
         * 设置自定义列宽度
         * 
         * @param columnIndex 列索引（从0开始）
         * @param width 列宽度
         * @return 当前对象，用于链式调用
         */
        public AlignmentOptions withCustomColumnWidth(int columnIndex, int width) {
            this.customColumnWidths.put(columnIndex, width);
            return this;
        }

        @Override
        public String toString() {
            return String.format(
                "AlignmentOptions{delimiterRegex='%s', commentMarker='%s', minColumnSpacing=%d, " +
                "preserveTrailingComma=%s, trimElements=%s, customColumnWidths=%s}",
                delimiterRegex, commentMarker, minColumnSpacing, 
                preserveTrailingComma, trimElements, customColumnWidths
            );
        }
    }

    /**
     * 对齐统计信息类
     */
    public static class AlignmentStats {
        /** 总行数 */
        public int totalLines;

        /** 最大列数 */
        public int maxColumns;

        /** 各列宽度数组 */
        public int[] columnWidths;

        /** 是否包含注释 */
        public boolean hasComments;

        /** 数据行数 */
        public int dataLines;

        /** 注释行数 */
        public int commentLines;

        /** 空行数 */
        public int emptyLines;

        @Override
        public String toString() {
            return String.format(
                "AlignmentStats{totalLines=%d, maxColumns=%d, columnWidths=%s, " +
                "hasComments=%s, dataLines=%d, commentLines=%d, emptyLines=%d}",
                totalLines, maxColumns, Arrays.toString(columnWidths), 
                hasComments, dataLines, commentLines, emptyLines
            );
        }
    }

    /**
     * 对齐多行文本的缩进（使用默认选项）
     *
     * @param lines 要对齐的文本行列表，不能为null但可以为空
     * @return 对齐后的文本行列表，不会返回null
     * @throws IllegalArgumentException 如果lines或mode为null
     * 
     * <p>示例：
     * <pre>
     * List<String> input = Arrays.asList("a,b", "c,d // comment");
     * List<String> result = IndentationAligner.align(input);
     * </pre>
     */
    public static List<String> align(List<String> lines) {
        return align(lines, Mode.FORMAT, new AlignmentOptions());
    }

    /**
     * 对齐单行文本的缩进（将单行文本转换为列表后处理，使用默认选项）
     *
     * @param singleLine 要处理的单行文本，不能为null但可以为空
     * @param mode 对齐模式，不能为null
     * @return 对齐后的文本行列表，通常只包含一个元素，不会返回null
     * @throws IllegalArgumentException 如果singleLine或mode为null
     * 
     * <p>示例：
     * <pre>
     * String result = IndentationAligner.align("a,b,c", Mode.FORMAT).get(0);
     * </pre>
     */
    public static List<String> align(String singleLine, Mode mode) {
        return align(singleLine, mode, new AlignmentOptions());
    }

    /**
     * 对齐单行文本的缩进（将单行文本转换为列表后处理，使用自定义选项）
     *
     * @param singleLine 要处理的单行文本，不能为null但可以为空
     * @param mode 对齐模式，不能为null
     * @param options 对齐选项，不能为null
     * @return 对齐后的文本行列表，通常只包含一个元素，不会返回null
     * @throws IllegalArgumentException 如果singleLine、mode或options为null
     */
    public static List<String> align(String singleLine, Mode mode, AlignmentOptions options) {
        if (singleLine == null) {
            throw new IllegalArgumentException("singleLine cannot be null");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode cannot be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null");
        }

        return align(Collections.singletonList(singleLine), mode, options);
    }

   /**
     * 对齐多行文本的缩进
     *
     * @param lines 要对齐的文本行列表，不能为null但可以为空
     * @return 对齐后的文本行列表，不会返回null
     * @throws IllegalArgumentException 如果lines或mode为null
     * 
     * <p>示例：
     * <pre>
     * List<String> input = Arrays.asList("a,b", "c,d // comment");
     * List<String> result = IndentationAligner.align(input, Mode.FORMAT);
     * </pre>
     */
    public static List<String> align(List<String> lines, Mode mode) {
        return align(lines, mode, new AlignmentOptions());
    }

    /**
     * 对齐多行文本的缩进（使用自定义选项）
     *
     * @param lines 要对齐的文本行列表，不能为null但可以为空
     * @param mode 对齐模式，不能为null
     * @param options 对齐选项，不能为null
     * @return 对齐后的文本行列表，不会返回null
     * @throws IllegalArgumentException 如果lines、mode或options为null
     * 
     * <p>示例：
     * <pre>
     * AlignmentOptions options = new AlignmentOptions().withMinSpacing(3);
     * List<String> result = IndentationAligner.align(lines, Mode.FORMAT, options);
     * </pre>
     */
    public static List<String> align(List<String> lines, Mode mode, AlignmentOptions options) {
        if (lines == null) {
            throw new IllegalArgumentException("lines cannot be null");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode cannot be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null");
        }

        if (lines.isEmpty()) {
            return new ArrayList<>();
        }

        // 解析所有行
        List<ParsedLine> parsedLines = parseLinesEnhanced(lines, options);

        // 计算每列的最大宽度
        int maxColumns = calculateMaxColumns(parsedLines);
        int[] columnWidths = calculateSmartColumnWidths(parsedLines, maxColumns, options);

        // 构建结果
        return buildEnhancedAlignedLines(parsedLines, columnWidths, maxColumns, mode, options);
    }

    /**
     * 对齐多行文本字符串（按换行符分割，使用默认选项）
     *
     * @param multiLineString 多行文本字符串，按换行符分割，不能为null但可以为空
     * @return 对齐后的文本行列表，不会返回null
     * @throws IllegalArgumentException 如果multiLineString或mode为null
     * 
     * <p>示例：
     * <pre>
     * String input = "apple,banana\\ncherry,dates // fruits";
     * List<String> result = IndentationAligner.alignFromString(input);
     * </pre>
     */
    public static List<String> alignFromString(String multiLineString) {
        return alignFromString(multiLineString, Mode.FORMAT, new AlignmentOptions());
    }

    /**
     * 对齐多行文本字符串（按换行符分割，使用默认选项）
     *
     * @param multiLineString 多行文本字符串，按换行符分割，不能为null但可以为空
     * @param mode 对齐模式，不能为null
     * @return 对齐后的文本行列表，不会返回null
     * @throws IllegalArgumentException 如果multiLineString或mode为null
     * 
     * <p>示例：
     * <pre>
     * String input = "apple,banana\\ncherry,dates // fruits";
     * List<String> result = IndentationAligner.alignFromString(input, Mode.FORMAT);
     * </pre>
     */
    public static List<String> alignFromString(String multiLineString, Mode mode) {
        return alignFromString(multiLineString, mode, new AlignmentOptions());
    }

    /**
     * 对齐多行文本字符串（按换行符分割，使用自定义选项）
     *
     * @param multiLineString 多行文本字符串，按换行符分割，不能为null但可以为空
     * @param mode 对齐模式，不能为null
     * @param options 对齐选项，不能为null
     * @return 对齐后的文本行列表，不会返回null
     * @throws IllegalArgumentException 如果multiLineString、mode或options为null
     */
    public static List<String> alignFromString(String multiLineString, Mode mode, AlignmentOptions options) {
        if (multiLineString == null) {
            throw new IllegalArgumentException("multiLineString cannot be null");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode cannot be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null");
        }

        if (multiLineString.isEmpty()) {
            return new ArrayList<>();
        }

        String[] lines = multiLineString.split("\\n");
        return align(Arrays.asList(lines), mode, options);
    }

    /**
     * 处理文件内容对齐
     *
     * @param inputFile 输入文件路径
     * @param outputFile 输出文件路径
     * @param mode 对齐模式
     * @param options 对齐选项
     * @throws IOException 如果文件读写失败
     * 
     * <p>示例：
     * <pre>
     * IndentationAligner.processFile("input.txt", "output.txt", Mode.FORMAT, new AlignmentOptions());
     * </pre>
     */
    public static void processFile(String inputFile, String outputFile, Mode mode, AlignmentOptions options) 
        throws IOException {
        if (inputFile == null || outputFile == null) {
            throw new IllegalArgumentException("inputFile and outputFile cannot be null");
        }

        List<String> lines = Files.readAllLines(Paths.get(inputFile), StandardCharsets.UTF_8);
        List<String> aligned = align(lines, mode, options);
        Files.write(Paths.get(outputFile), aligned, StandardCharsets.UTF_8);
    }

    /**
     * 获取对齐统计信息
     *
     * @param lines 文本行列表
     * @param mode 对齐模式
     * @param options 对齐选项
     * @return 对齐统计信息
     * 
     * <p>示例：
     * <pre>
     * AlignmentStats stats = IndentationAligner.getAlignmentStats(lines, Mode.FORMAT, options);
     * System.out.println("最大列数: " + stats.maxColumns);
     * </pre>
     */
    public static AlignmentStats getAlignmentStats(List<String> lines, Mode mode, AlignmentOptions options) {
        if (lines == null || mode == null || options == null) {
            throw new IllegalArgumentException("Params cannot be null");
        }

        List<ParsedLine> parsedLines = parseLinesEnhanced(lines, options);
        int maxColumns = calculateMaxColumns(parsedLines);
        int[] widths = calculateSmartColumnWidths(parsedLines, maxColumns, options);

        AlignmentStats stats = new AlignmentStats();
        stats.totalLines = lines.size();
        stats.maxColumns = maxColumns;
        stats.columnWidths = widths;
        stats.hasComments = parsedLines.stream().anyMatch(pl -> pl.hasComment);
        stats.dataLines = (int) parsedLines.stream().filter(pl -> pl.hasData).count();
        stats.commentLines = (int) parsedLines.stream().filter(pl -> pl.hasComment && !pl.hasData).count();
        stats.emptyLines = (int) parsedLines.stream().filter(pl -> !pl.hasData && !pl.hasComment).count();

        return stats;
    }

    /**
     * 增强的解析方法，支持更多分隔符和引号感知
     *
     * @param lines 原始文本行列表
     * @param options 对齐选项
     * @return 解析后的行信息列表
     */
    private static List<ParsedLine> parseLinesEnhanced(List<String> lines, AlignmentOptions options) {
        List<ParsedLine> result = new ArrayList<>();
        Pattern delimiterPattern = Pattern.compile(options.delimiterRegex);

        for (String line : lines) {
            ParsedLine parsed = new ParsedLine();
            parsed.originalLine = line;

            // 分离注释（考虑引号内的注释标记）
            String[] commentSplit = splitComment(line, options.commentMarker);
            parsed.dataPart = commentSplit[0].trim();
            parsed.commentPart = commentSplit[1];
            parsed.hasComment = !parsed.commentPart.isEmpty();

            // 解析数据部分
            if (!parsed.dataPart.isEmpty()) {
                parsed.elements = parseElements(parsed.dataPart, delimiterPattern, options.trimElements);
                parsed.hasData = !parsed.elements.isEmpty();

                // 检测尾随逗号
                parsed.hasTrailingComma = options.preserveTrailingComma && parsed.dataPart.trim().endsWith(",");
            } else {
                parsed.elements = new ArrayList<>();
                parsed.hasData = false;
                parsed.hasTrailingComma = false;
            }

            result.add(parsed);
        }

        return result;
    }

    /**
     * 智能注释分离，避免分割引号内的注释标记
     *
     * @param line 原始行
     * @param commentMarker 注释标记
     * @return 分离后的数据部分和注释部分
     */
    private static String[] splitComment(String line, String commentMarker) {
        if (!line.contains(commentMarker)) {
            return new String[]{line, ""};
        }

        // 简单的引号处理
        boolean inQuotes = false;
        char quoteChar = '"';

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"' || c == '\'') {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
            }

            if (!inQuotes && i + commentMarker.length() <= line.length()) {
                String substring = line.substring(i, i + commentMarker.length());
                if (substring.equals(commentMarker)) {
                    return new String[]{
                        line.substring(0, i).trim(),
                        line.substring(i).trim()
                    };
                }
            }
        }

        return new String[]{line, ""};
    }

    /**
     * 解析元素列表
     *
     * @param dataPart 数据部分
     * @param delimiterPattern 分隔符模式
     * @param trimElements 是否修剪元素
     * @return 元素列表
     */
    private static List<String> parseElements(String dataPart, Pattern delimiterPattern, boolean trimElements) {
        if (dataPart.isEmpty()) {
            return new ArrayList<>();
        }

        String[] elements = delimiterPattern.split(dataPart);
        return Arrays.stream(elements)
            .filter(e -> !e.trim().isEmpty())
            .map(e -> trimElements ? e.trim() : e)
            .collect(Collectors.toList());
    }

    /**
     * 计算最大列数
     *
     * @param lines 解析后的行列表
     * @return 最大列数
     */
    private static int calculateMaxColumns(List<ParsedLine> lines) {
        return lines.stream()
            .mapToInt(line -> line.elements.size())
            .max()
            .orElse(0);
    }

    /**
     * 智能列宽计算，考虑内容类型和可读性
     *
     * @param lines 解析后的行列表
     * @param maxColumns 最大列数
     * @param options 对齐选项
     * @return 每列的最大宽度数组
     */
    private static int[] calculateSmartColumnWidths(List<ParsedLine> lines, int maxColumns, AlignmentOptions options) {
        int[] widths = new int[maxColumns];
        boolean[] isNumeric = new boolean[maxColumns];
        int[] sampleCount = new int[maxColumns];

        // 初始化
        Arrays.fill(widths, 0);
        Arrays.fill(isNumeric, true);
        Arrays.fill(sampleCount, 0);

        // 分析每列的数据特征
        for (ParsedLine line : lines) {
            for (int i = 0; i < line.elements.size() && i < maxColumns; i++) {
                String element = line.elements.get(i);
                widths[i] = Math.max(widths[i], element.length());
                sampleCount[i]++;

                // 检查是否为数值（简单的启发式检查）
                if (isNumeric[i] && !isLikelyNumeric(element)) {
                    isNumeric[i] = false;
                }
            }
        }

        // 应用自定义宽度和智能调整
        for (int i = 0; i < maxColumns; i++) {
            if (options.customColumnWidths.containsKey(i)) {
                widths[i] = options.customColumnWidths.get(i);
            } else if (sampleCount[i] > 0) {
                // 为数值列添加额外空格以提高可读性
                if (isNumeric[i]) {
                    widths[i] += 1;
                }
            }
        }

        return widths;
    }

    /**
     * 检查字符串是否可能为数值
     *
     * @param str 输入字符串
     * @return 如果可能为数值则返回true
     */
    private static boolean isLikelyNumeric(String str) {
        if (str == null || str.isEmpty()) return false;

        // 简单的数值模式匹配
        return str.matches("^-?\\d+(\\.\\d+)?$") || 
               str.matches("^-?\\d+(\\.\\d+)?[eE][+-]?\\d+$");
    }

    /**
     * 构建增强的对齐行
     *
     * @param parsedLines 解析后的行列表
     * @param columnWidths 列宽度数组
     * @param maxColumns 最大列数
     * @param mode 对齐模式
     * @param options 对齐选项
     * @return 对齐后的文本行列表
     */
    private static List<String> buildEnhancedAlignedLines(List<ParsedLine> parsedLines, 
    int[] columnWidths, 
    int maxColumns, 
    Mode mode,
    AlignmentOptions options) {
        List<String> result = new ArrayList<>();
        int commentColumn = calculateEnhancedCommentColumn(parsedLines, columnWidths, maxColumns, mode, options);

        for (ParsedLine parsed : parsedLines) {
            StringBuilder lineBuilder = new StringBuilder();

            if (parsed.hasData) {
                // 构建数据部分
                buildEnhancedDataPart(lineBuilder, parsed, columnWidths, mode, options);

                // 添加注释
                if (parsed.hasComment) {
                    alignComment(lineBuilder, parsed.commentPart, commentColumn);
                } else if (parsed.hasTrailingComma && options.preserveTrailingComma && mode == Mode.FORMAT) {
                    // 处理尾随逗号
                    lineBuilder.append(",");
                }
            } else {
                // 处理无数据行
                handleEmptyDataLine(lineBuilder, parsed);
            }

            result.add(lineBuilder.toString());
        }

        return result;
    }

    /**
     * 计算增强的注释对齐列位置
     *
     * @param parsedLines 解析后的行列表
     * @param columnWidths 列宽度数组
     * @param maxColumns 最大列数
     * @param mode 对齐模式
     * @param options 对齐选项
     * @return 注释对齐的列位置
     */
    private static int calculateEnhancedCommentColumn(List<ParsedLine> parsedLines, 
    int[] columnWidths, 
    int maxColumns, 
    Mode mode,
    AlignmentOptions options) {
        int maxDataWidth = 0;

        for (ParsedLine parsed : parsedLines) {
            if (parsed.hasData) {
                int lineWidth = calculateLineWidth(parsed.elements, columnWidths, mode);
                if (lineWidth > maxDataWidth) {
                    maxDataWidth = lineWidth;
                }
            }
        }

        // 注释从数据部分结束后至少指定个数的空格开始
        return maxDataWidth + options.minColumnSpacing;
    }

    /**
     * 计算一行的数据部分宽度
     *
     * @param elements 元素列表
     * @param columnWidths 列宽度数组
     * @param mode 对齐模式
     * @return 数据部分的总宽度
     */
    private static int calculateLineWidth(List<String> elements, int[] columnWidths, Mode mode) {
        if (elements.isEmpty()) return 0;

        int width = 0;

        for (int i = 0; i < elements.size(); i++) {
            // 元素本身的宽度（对齐到列宽）
            width += columnWidths[i];

            // 添加分隔符的宽度
            if (i < elements.size() - 1) {
                if (mode == Mode.FORMAT) {
                    width += 2; // 逗号 + 空格
                } else {
                    width += 1; // 空格
                }
            }
        }

        return width;
    }

    /**
     * 构建增强的数据部分
     *
     * @param builder 字符串构建器
     * @param parsed 解析后的行信息
     * @param columnWidths 列宽度数组
     * @param mode 对齐模式
     * @param options 对齐选项
     */
    private static void buildEnhancedDataPart(StringBuilder builder, 
    ParsedLine parsed,
    int[] columnWidths, 
    Mode mode,
    AlignmentOptions options) {
        List<String> elements = parsed.elements;

        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);

            // 添加元素，左对齐到列宽
            builder.append(element);
            int padding = columnWidths[i] - element.length();
            for (int j = 0; j < padding; j++) {
                builder.append(' ');
            }

            // 添加分隔符（最后一个元素不加）
            if (i < elements.size() - 1) {
                if (mode == Mode.FORMAT) {
                    builder.append(", ");
                } else {
                    builder.append(' ');
                }
            }
        }
    }

    /**
     * 对齐注释
     *
     * @param builder 字符串构建器
     * @param comment 注释内容
     * @param commentColumn 注释列位置
     */
    private static void alignComment(StringBuilder builder, String comment, int commentColumn) {
        // 确保数据部分和注释之间有足够的空格
        int currentLength = builder.length();
        int spacesNeeded = Math.max(1, commentColumn - currentLength);

        for (int i = 0; i < spacesNeeded; i++) {
            builder.append(' ');
        }

        builder.append(comment);
    }

    /**
     * 处理无数据行
     *
     * @param builder 字符串构建器
     * @param parsed 解析后的行信息
     */
    private static void handleEmptyDataLine(StringBuilder builder, ParsedLine parsed) {
        if (parsed.hasComment) {
            // 未来扩展为按需去除注释
            builder.append(parsed.commentPart);
        } else {
            // 空行，保持原样
            builder.append(parsed.originalLine);
        }
    }

    /**
     * 解析后的行信息内部类
     */
    private static class ParsedLine {
        /** 原始行内容 */
        String originalLine;
        /** 数据部分 */
        String dataPart;
        /** 注释部分 */
        String commentPart;
        /** 解析出的元素列表 */
        List<String> elements;
        /** 是否包含数据 */
        boolean hasData;
        /** 是否包含注释 */
        boolean hasComment;
        /** 是否有尾随逗号 */
        boolean hasTrailingComma;

        @Override
        public String toString() {
            return String.format(
                "ParsedLine{elements=%s, comment='%s', hasData=%s, hasComment=%s, hasTrailingComma=%s}",
                elements, commentPart, hasData, hasComment, hasTrailingComma
            );
        }
    }
}