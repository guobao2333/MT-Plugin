package guobao.plugin.converter;

import android.content.SharedPreferences;
import java.io.*;
import java.util.regex.*;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

import guobao.plugin.converter.util.*;


public class Converter {

    private LocalString string;
    private MTPluginContext context;
    private SharedPreferences config;

    public Converter(MTPluginContext context) {
        this.context = context;
        this.config = context.getPreferences();
        this.string = context.getAssetLocalString("String");
    }

    public void main(String[] args) {
        // context.showToast("开始转换……");
    }

    public String convert(String t, String tool, String to) throws IOException {
        switch (tool) {
            /* case "md_ubb":
                return "ubb".equals(to) ? toUBB(t) : toMarkdown(t); */

            case "case": return strCase(t, to);
            case "unicode": return unicode(t, to);
            case "zshh": return zshh(t, to);
        }
        return "ERROR";
    }

    public String md_ubb(String str) throws IOException {
    		return "";
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
        ZshHist zsh = new ZshHist(context);
        String outputPath = source + "_new";
        zsh.process(source, outputPath, "encode".equals(target));

        return string.get(target) + string.get("zshh_out") + outputPath;
    }

    public String strCase(String str, String to) throws IOException {
        if (str.isEmpty()) return str;

        switch (to) {
            case "upper": return str.toUpperCase();
            case "lower": return str.toLowerCase();

            case "snake": // 驼峰转蛇形
                String regex = "(?<=[a-z])(?=[A-Z])" 
                  + (config.getBoolean("snake_upper_continuous", false) ? "|(?<=[A-Z])(?=[A-Z][a-z])" : "")
                  + (config.getBoolean("snake_number", false) ? "|(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)" : "");

                return str.replaceAll(regex, "_").toLowerCase(); 

            case "camel":
                String[] parts = str.split("_");
                if (parts.length == 0) return str;

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (part.isEmpty()) continue;

                    String firstChar = part.substring(0, 1).toUpperCase();
                    String rest = part.length() > 1 ? part.substring(1).toLowerCase() : "";

                    if (i == 0 && !config.getBoolean("camel_upper", false)) {
                        // 小驼峰首单词首字母小写
                        sb.append(part.toLowerCase());
                    } else {
                        sb.append(firstChar).append(rest);
                    }
                }
                return sb.toString();

            case "kebab":
            case "space":
            default: return "正在开发中……";
        }
    }
}