package guobao.plugin.converter.util;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;

import java.util.*;

/**
 * Markdown ↔ UBB 双向转换器
 *
 * - Markdown→UBB 使用 commonmark-java 解析 AST 并自定义渲染器输出 UBB。
 * - UBB→Markdown 使用简单的解析/替换策略，覆盖常见标签（b/i/u/s/url/img/quote/code/list 等）。
 * - 仅实现通用语法，不同论坛的UBB可能存在差异，后续拓展。
 */
public class MarkdownUbbConverter {

    private final Parser mdParser;

    public MarkdownUbbConverter() {
        List<Extension> exts = new ArrayList<>();
        exts.add(StrikethroughExtension.create());
        mdParser = Parser.builder().extensions(exts).build();
    }

    // ---------------- Markdown -> UBB ----------------
    public String toUBB(String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        Node doc = mdParser.parse(markdown);
        StringBuilder out = new StringBuilder();
        renderNodeToUbb(doc, out);
        // 去掉结尾多余空行
        return trimExtraBlankLines(out.toString());
    }

    private void renderNodeToUbb(Node node, StringBuilder out) {
        node.accept(new AbstractVisitor() {
            @Override public void visit(Document document) { visitChildren(document); }
            @Override public void visit(Paragraph paragraph) { visitChildren(paragraph); out.append("\n\n"); }
            @Override public void visit(Text text) { out.append(escapeUbb(text.getLiteral())); }
            @Override public void visit(SoftLineBreak softLineBreak) { out.append("\n"); }
            @Override public void visit(HardLineBreak hardLineBreak) { out.append("\n"); }
            @Override public void visit(Emphasis emphasis) { out.append("[i]"); visitChildren(emphasis); out.append("[/i]"); }
            @Override public void visit(StrongEmphasis strongEmphasis) { out.append("[b]"); visitChildren(strongEmphasis); out.append("[/b]"); }
            @Override public void visit(BlockQuote blockQuote) { out.append("[quote]"); visitChildren(blockQuote); out.append("[/quote]\n\n"); }
            @Override public void visit(Code code) { out.append("[code]").append(code.getLiteral()).append("[/code]"); }
            @Override public void visit(FencedCodeBlock fencedCodeBlock) { out.append("[code]"); out.append(fencedCodeBlock.getLiteral()); out.append("[/code]\n\n"); }
            @Override public void visit(IndentedCodeBlock indentedCodeBlock) { out.append("[code]"); out.append(indentedCodeBlock.getLiteral()); out.append("[/code]\n\n"); }
            @Override public void visit(Heading heading) {
                int level = heading.getLevel();
                // Java 8+
                // int percent = switch (level) { case 1 -> 200; case 2 -> 170; case 3 -> 150; case 4 -> 130; case 5 -> 115; default -> 100; };

                // Java 7-
                int percent;
                switch (level) {
                    case 1: percent = 200; break;
                    case 2: percent = 170; break;
                    case 3: percent = 150; break;
                    case 4: percent = 130; break;
                    case 5: percent = 115; break;
                    default: percent = 100; break;
                }
                out.append("[size=").append(percent).append("%][b]"); visitChildren(heading); out.append("[/b][/size]\n\n");
            }
            @Override public void visit(ThematicBreak thematicBreak) { out.append("[hr]\n\n"); }
            @Override public void visit(BulletList bulletList) { out.append("[list]\n"); visitChildren(bulletList); out.append("[/list]\n\n"); }
            @Override public void visit(OrderedList orderedList) { out.append("[list=1]\n"); visitChildren(orderedList); out.append("[/list]\n\n"); }
            @Override public void visit(ListItem listItem) { out.append("[*]"); visitChildren(listItem); if (!endsWithNewline(out)) out.append('\n'); }
            @Override public void visit(HtmlInline htmlInline) { out.append(escapeUbb(htmlInline.getLiteral())); }
            @Override public void visit(HtmlBlock htmlBlock) { out.append(escapeUbb(htmlBlock.getLiteral())).append("\n\n"); }
            @Override public void visit(CustomNode customNode) {
                if (customNode instanceof Strikethrough) {
                    out.append("[s]"); visitChildren(customNode); out.append("[/s]");
                } else {
                    visitChildren(customNode);
                }
            }
        });
    }

    // ---------------- UBB -> Markdown（AST） ----------------
    public String toMarkdown(String ubb) {
        if (ubb == null || ubb.isEmpty()) return "";
        String s = ubb.replace("\n", "\n").replace("\n", "\n");
        UbbNode root = new UbbParser(s).parse();
        String md = new UbbRenderer().render(root);
        return md.replaceAll("\n{3,}", "\n\n").trim() + "\n";
    }

    // ---------------- UBB AST ----------------
    private static class UbbNode {
        String tag; // null 表示文本节点
        Map<String, String> attrs = new LinkedHashMap<>();
        List<UbbNode> children = new ArrayList<>();
        String text; // 仅文本节点使用

        static UbbNode text(String t) { UbbNode n = new UbbNode(); n.tag = null; n.text = t; return n; }
        static UbbNode tag(String tagName) { UbbNode n = new UbbNode(); n.tag = tagName.toLowerCase(); return n; }
        boolean isText() { return tag == null; }

        @Override public String toString() { if (isText()) return "TEXT(" + text + ")"; return "TAG(" + tag + ",children=" + children.size() + ")"; }
    }

    // ---------------- 简易 UBB 解析器 ----------------
    private static class UbbParser {
        final String s; int i = 0; int n;
        UbbParser(String s) { this.s = s; this.n = s.length(); }

        UbbNode parse() {
            UbbNode root = UbbNode.tag("root");
            while (i < n) root.children.add(parseNode());
            return root;
        }

        private UbbNode parseNode() {
            if (peek() == '[') {
                int save = i;
                String tag = parseTagNameForOpenOrClose();
                if (tag == null) { i = save; return parseTextUntil('['); }
                if (tag.startsWith("/")) { return UbbNode.text("[" + tag + "]"); }
                i = save;
                TagInfo open = readOpenTag();
                if (open == null) return parseTextUntil('[');
                UbbNode node = UbbNode.tag(open.name);
                node.attrs.putAll(open.attrs);
                while (i < n) {
                    int before = i;
                    if (peek() == '[') {
                        int j = i + 1;
                        // 1) list item 的隐式结束：遇到下一个 [*] 或 [/list]，当前 [*] 结束
                        if ("*".equals(open.name)) {
                            int br = s.indexOf(']', j);
                            if (br != -1) {
                                String inner = s.substring(j, br).trim();
                                String lower = inner.toLowerCase(Locale.ROOT);
                                if ("*".equals(lower) || "/list".equals(lower)) break; // 让父级处理
                            }
                        }
                        // 2) 常规的匹配关闭标签
                        if (j < n && s.charAt(j) == '/') {
                            int k = j + 1;
                            String name = readNameAt(k);
                            if (name != null) {
                                int afterName = k + name.length();
                                while (afterName < n && s.charAt(afterName) != ']') afterName++;
                                if (afterName < n && s.charAt(afterName) == ']' && name.equalsIgnoreCase(open.name)) i = afterName + 1; break;
                            }
                        }
                    }
                    if (i >= n) break;
                    UbbNode child = parseNode();
                    if (i == before) { i++; node.children.add(UbbNode.text(s.substring(before, i))); }
                    else node.children.add(child);
                }
                return node;
            } else return parseTextUntil('[');
        }

        private UbbNode parseTextUntil(char stop) {
            int start = i; while (i < n && s.charAt(i) != stop) i++; return UbbNode.text(s.substring(start, i));
        }

        private char peek() { return i < n ? s.charAt(i) : '\0'; }

        private static class TagInfo { String name; Map<String,String> attrs = new LinkedHashMap<>(); }

        private TagInfo readOpenTag() {
            if (peek() != '[') return null;
            int start = i; i++;
            int nameStart = i; while (i < n && isNameChar(s.charAt(i))) i++;
            if (i >= n) { i = start; return null; }
            String name = s.substring(nameStart, i);
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            TagInfo info = new TagInfo(); info.name = name.toLowerCase(Locale.ROOT);
            while (i < n && s.charAt(i) != ']') {
                if (s.charAt(i) == '=') {
                    i++; int vstart = i; while (i < n && s.charAt(i) != ']') i++; String val = s.substring(vstart, i).trim(); info.attrs.put("", val);
                } else if (isAttrKeyChar(s.charAt(i))) {
                    int kstart = i; while (i < n && isAttrKeyChar(s.charAt(i))) i++; String key = s.substring(kstart, i).toLowerCase(Locale.ROOT); while (i < n && Character.isWhitespace(s.charAt(i))) i++;
                    if (i < n && s.charAt(i) == '=') {
                        i++;
                        if (i < n && (s.charAt(i) == '"' || s.charAt(i) == '\'')) {
                            char q = s.charAt(i++); int vs = i; while (i < n && s.charAt(i) != q) i++; String val = s.substring(vs, Math.min(i, n)); if (i < n) i++; info.attrs.put(key, val);
                        } else {
                            int vstart = i; while (i < n && s.charAt(i) != ']' && !Character.isWhitespace(s.charAt(i))) i++; String val = s.substring(vstart, i).trim(); info.attrs.put(key, val);
                        }
                    } else { info.attrs.put(key, ""); }
                } else { i++; }
            }
            if (i < n && s.charAt(i) == ']') i++; else { i = start; return null; }
            return info;
        }

        private static boolean isNameChar(char c) { return Character.isLetterOrDigit(c) || c == '*' || c == '-'; }
        private static boolean isAttrKeyChar(char c) { return Character.isLetterOrDigit(c) || c == '-' || c == '_'; }
        private String readNameAt(int k) { if (k >= n) return null; int j = k; while (j < n && Character.isLetterOrDigit(s.charAt(j))) j++; if (j == k) return null; return s.substring(k, j); }
        private String parseTagNameForOpenOrClose() { if (peek() != '[') return null; int tmp = i + 1; if (tmp >= n) return null; int br = s.indexOf(']', tmp); if (br == -1) return null; String inner = s.substring(tmp, br); if (inner.length() == 0) return null; return inner.trim(); }
    }

    // ---------------- UBB AST -> Markdown 渲染器（支持 Discuz 扩展） ----------------
    private static class UbbRenderer {
        StringBuilder out = new StringBuilder();

        String render(UbbNode root) {
            for (UbbNode c : root.children) renderNode(c);
            return out.toString();
        }

        void renderNode(UbbNode node) {
            if (node.isText()) { out.append(node.text); return; }
            String tag = node.tag == null ? "" : node.tag.toLowerCase(Locale.ROOT);
            switch (tag) {
                case "root": for (UbbNode c : node.children) renderNode(c); break;
                // Java 8+
                /* case "b": wrap("**", () -> renderChildren(node)); break;
                case "i": wrap("*", () -> renderChildren(node)); break;
                case "s":
                case "del": wrap("~~", () -> renderChildren(node)); break; */

                // Java 7-
                case "b": 
                    wrap("**", new Runnable() {
                        @Override
                        public void run() { renderChildren(node); }
                    }); 
                    break;
                case "i": 
                    wrap("*", new Runnable() {
                        @Override
                        public void run() { renderChildren(node); }
                    }); 
                    break;
                case "u": out.append("<u>"); renderChildren(node); out.append("</u>"); break;
                case "s":
                case "del": 
                    wrap("~~", new Runnable() {
                        @Override
                        public void run() { renderChildren(node); }
                    }); 
                    break;
                case "code": renderCode(node); break;
                case "img": renderImage(node); break;
                case "url": renderUrl(node); break;
                case "email": renderEmail(node); break;
                case "quote": renderQuote(node); break;
                case "hide": renderHide(node); break;
                case "list": renderList(node); break;
                case "*":
                case "li": renderListItem(node); break;
                case "table": renderTable(node); break;
                case "tr": renderTableRow(node); break;
                case "td": renderTableCell(node); break;
                case "attach": renderAttach(node); break;
                case "media":
                case "audio":
                case "video": renderMedia(node); break;
                case "size": renderSize(node); break;
                case "hr": out.append("\n---\n"); break;
                default:
                    if (isInlineHtmlLike(tag)) { out.append(nodeToHtml(node)); }
                    else { out.append(nodeToUbb(node)); }
            }
        }

        private void renderChildren(UbbNode node) { for (UbbNode c : node.children) renderNode(c); }
        private void wrap(String marker, Runnable r) { out.append(marker); r.run(); out.append(marker); }

        private void renderCode(UbbNode node) {
            String inner = renderToString(node.children).replaceAll("\n", "\n");
            if (inner.contains("\n")) { out.append("\n```\n").append(inner).append("\n```\n"); }
            else { out.append('`').append(inner).append('`'); }
        }

        private void renderImage(UbbNode node) {
            String src = attr(node.attrs, "", null);
            if (src == null) { String inner = renderToString(node.children).trim(); if (!inner.isEmpty()) src = inner; }
            if (src == null || src.isEmpty()) return; out.append("![](").append(src).append(")");
        }

        private void renderUrl(UbbNode node) {
            String href = attr(node.attrs, "", null);
            String text = renderToString(node.children).trim(); if (href == null || href.isEmpty()) href = text; if (text.isEmpty()) text = href; out.append('[').append(text).append(']').append('(').append(href).append(')');
        }

        private void renderEmail(UbbNode node) {
            String mail = attr(node.attrs, "", null); if (mail == null) mail = renderToString(node.children).trim(); if (mail == null || mail.isEmpty()) return; out.append('[').append(mail).append(']').append('(').append("mailto:").append(mail).append(')');
        }

        private void renderQuote(UbbNode node) {
            String author = attr(node.attrs, "", null);
            String inner = renderToString(node.children).trim();
            String[] lines = inner.split("\n");
            out.append('\n');
            if (author != null && !author.isEmpty()) out.append("> **").append(author).append(":**\n");
            for (String line : lines) { out.append("> ").append(line).append('\n'); }
            out.append('\n');
        }

        private void renderHide(UbbNode node) {
            String inner = renderToString(node.children).trim();
            out.append("\n<details><summary>隐藏内容</summary>\n\n").append(inner).append("\n\n</details>\n\n");
        }

        private void renderList(UbbNode node) {
            String primary = attr(node.attrs, "", null);
            boolean ordered = primary != null && !primary.isEmpty() && !"0".equals(primary);
            int idx = 1; out.append('\n');
            for (UbbNode child : node.children) {
                if (child.tag != null && (child.tag.equals("*") || child.tag.equals("li"))) {
                    String item = renderToString(child.children).trim(); if (item.isEmpty()) continue;
                    String prefix = ordered ? (idx++) + ". " : "- ";
                    item = item.replaceAll("\n", "\n  ");
                    out.append(prefix).append(item).append('\n');
                } else {
                    String item = renderToString(Collections.singletonList(child)).trim(); if (item.isEmpty()) continue;
                    String prefix = ordered ? (idx++) + ". " : "- ";
                    item = item.replaceAll("\n", "\n  ");
                    out.append(prefix).append(item).append('\n');
                }
            }
            out.append('\n');
        }

        private void renderListItem(UbbNode node) { renderChildren(node); }

        private void renderTable(UbbNode node) {
            List<List<String>> rows = new ArrayList<>();
            for (UbbNode child : node.children) {
                if ("tr".equals(child.tag)) {
                    List<String> cells = new ArrayList<>();
                    for (UbbNode td : child.children) if ("td".equals(td.tag)) cells.add(renderToString(td.children).trim());
                    if (!cells.isEmpty()) rows.add(cells);
                } else if ("td".equals(child.tag)) {
                    rows.add(Collections.singletonList(renderToString(child.children).trim()));
                }
            }
            if (rows.isEmpty()) return;
            List<String> header = rows.get(0);
            out.append('\n');
            out.append("| "); for (String h : header) out.append(h).append(" | "); out.append('\n');
            out.append("| "); for (int i=0;i<header.size();i++) out.append("--- | "); out.append('\n');
            for (int r = 1; r < rows.size(); r++) { List<String> row = rows.get(r); out.append("| "); for (String c : row) out.append(c).append(" | "); out.append('\n'); }
            out.append('\n');
        }

        private void renderTableRow(UbbNode node) { /* handled in table */ }
        private void renderTableCell(UbbNode node) { out.append(renderToString(node.children)); }

        private void renderAttach(UbbNode node) {
            String primary = attr(node.attrs, "", null);
            String inner = renderToString(node.children).trim();
            if (primary != null && !primary.isEmpty()) out.append("[附件:").append(primary).append("]");
            else if (!inner.isEmpty()) out.append("[附件] ").append(inner);
        }

        private void renderMedia(UbbNode node) {
            String inner = renderToString(node.children).trim(); if (inner.isEmpty()) return; out.append('[').append(node.tag).append(']').append('(').append(inner).append(')');
        }

        private void renderSize(UbbNode node) {
            String primary = attr(node.attrs, "", attr(node.attrs, "size", null));
            Integer level = primary == null ? null : mapSizeToHeading(primary);
            if (level != null && node.children.size() == 1 && node.children.get(0).tag != null && node.children.get(0).tag.equals("b")) {
                String content = renderToString(node.children.get(0).children).trim();
                // out.append('\n').append("#".repeat(level)).append(' ').append(content).append('\n\n');
                out.append('\n');
                for (int j = 0; j < level; j++) out.append('#');
                out.append(' ').append(content).append("\n\n");
                return;
            }
            out.append("<span style=\"font-size:").append(primary == null ? "" : primary).append("\">" ).append(renderToString(node.children)).append("</span>");
        }

        private static Integer mapSizeToHeading(String size) {
            String s = size.trim();
            // if (s.endsWith("%")) { try { int v = Integer.parseInt(s.substring(0, s.length()-1)); return switch (v) { case 200 -> 1; case 170 -> 2; case 150 -> 3; case 130 -> 4; case 115 -> 5; default -> 6; }; } catch (NumberFormatException e) { return null; } }
            // Java 7-
            if (s.endsWith("%")) { try { int v = Integer.parseInt(s.substring(0, s.length()-1)); switch (v) { case 200: return 1; case 170: return 2; case 150: return 3; case 130: return 4; case 115: return 5; default: return 6; }} catch (NumberFormatException e) { return null; }
}
            try { int v = Integer.parseInt(s); if (v >= 1 && v <= 6) return v; if (v == 200) return 1; if (v == 170) return 2; if (v == 150) return 3; if (v == 130) return 4; if (v == 115) return 5; } catch (NumberFormatException ignored) {}
            return null;
        }

        private String renderToString(List<UbbNode> nodes) {
            StringBuilder prev = out; out = new StringBuilder(); for (UbbNode c : nodes) renderNode(c); String res = out.toString(); out = prev; return res;
        }

        private boolean isInlineHtmlLike(String tag) { return tag.equals("color") || tag.equals("font") || tag.equals("align"); }

        private String nodeToHtml(UbbNode node) {
            if ("color".equals(node.tag)) { String v = attr(node.attrs, "", ""); return "<span style=\"color:" + v + "\">" + renderToString(node.children) + "</span>"; }
            if ("font".equals(node.tag)) { String v = attr(node.attrs, "", ""); return "<span style=\"font-family:" + v + "\">" + renderToString(node.children) + "</span>"; }
            if ("align".equals(node.tag)) { String v = attr(node.attrs, "", ""); return "<div align=\"" + v + "\">" + renderToString(node.children) + "</div>"; }
            return nodeToUbb(node);
        }

        private String nodeToUbb(UbbNode node) {
            StringBuilder b = new StringBuilder();
            b.append('[').append(node.tag);
            // 使用 keySet() 替代 entrySet() 来避免 Map.Entry 问题
            for (String key : node.attrs.keySet()) { 
                String value = node.attrs.get(key);
                if (key.isEmpty()) b.append('=').append(value); else b.append(' ').append(key).append('=').append(value); 
            }
            
            b.append(']'); 
            for (UbbNode c : node.children) { if (c.isText()) b.append(c.text); else b.append(nodeToUbb(c)); } 
            b.append("[/").append(node.tag).append("]"); 
            return b.toString();
        }
    }

    // ---------------- 简单工具 ----------------
    private static boolean endsWithNewline(StringBuilder sb) { int len = sb.length(); return len > 0 && sb.charAt(len - 1) == '\n'; }
    private static String trimExtraBlankLines(String s) { return s.replaceAll("\n{3,}", "\n\n").trim() + "\n"; }
    private static String escapeUbb(String text) { return text.replace("[", "&#91;").replace("]", "&#93;"); }
    /** 替代 Map.getOrDefault(key, def)
     *
     * 更新至Java 11语法：
     * 查找：attr\(node.attrs, (.+?)\);
     * 替换：node.attrs.getOrDefault($1);
     */ 
    private static String attr(java.util.Map<String, String> map, String key, String def) {
        if (map == null) return def;
        String v = map.get(key);
        return v != null ? v : def;
    }

}
