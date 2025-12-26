package com.example.myplugin;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class MyPreference implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        // 设置标题
        builder.title("插件设置");

        // 添加分组标题
        builder.addHeader("基础设置");

        // 添加选项
        builder.addText("Hello World")
                .summary("你好世界");
    }
}