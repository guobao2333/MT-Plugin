package guobao.plugin.converter;

import android.content.SharedPreferences;
import java.io.*;

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
            case "case":
                return to.equals("upper") ? t.toUpperCase() : t.toLowerCase();
            case "zshh": return zshh(t, to);
        }

        return "ERROR";
    }

    public String zshh(String source, String target) throws IOException {
        ZshHist zsh = new ZshHist(context);
        boolean metafy = target.equals("decode") ? false : true;
        String outputPath = source + "_new";
        zsh.process(source, outputPath, metafy);

        return string.get(target) + string.get("zshh_out") + outputPath;
    }
}