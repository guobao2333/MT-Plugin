package bin.mt.plugin.demo.preference;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class ExampleMain implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("MT插件功能演示");
        builder.addText("插件上下文").summary("PluginContext")
                .onClick((pluginUI, item) -> context.openPreference(ExampleContext.class));
        builder.addText("设置界面").summary("PluginPreference")
                .onClick((pluginUI, item) -> context.openPreference(ExamplePreference.class));
        builder.addText("UI 组件").summary("PluginUI")
                .onClick((pluginUI, item) -> context.openPreference(ExampleUI.class));
        builder.addText("对话框").summary("PluginDialog")
                .onClick((pluginUI, item) -> context.openPreference(ExampleDialog.class));
    }
}
