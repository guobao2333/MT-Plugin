package bin.mt.plugin.demo.preference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginView;

public class ExampleContext implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.addText("基本信息").summary("PluginContext").onClick((pluginUI, item) -> pluginUI.showMessage("基本信息",
                "SDKVersion: " + PluginContext.SDK_VERSION + "\n" +
                        "PluginID: " + context.getPluginId() + "\n" +
                        "VersionCode: " + context.getPluginVersionCode() + "\n" +
                        "VersionName: " + context.getPluginVersionName() + "\n" +
                        "Locale: " + context.getLanguageCountry()
        ));
        builder.addText("获取本地化文本").summary("getString").onClick((pluginUI, item) -> {
            String text = "getString('{key}') = '" + context.getString("{key}") + "'\n" +
                    "getString('key') = '" + context.getString("key") + "'\n" +
                    "getString('{example:key}') = '" + context.getString("{example:key}") + "'\n";
            text = text.replace("'", "\"");

            PluginView pluginView = pluginUI.buildVerticalLayout()
                    .addEditText().text(text)
                    .singleLine(false).readOnly()
                    .syntaxHighlight("Java")
                    .textSize(13).build();

            pluginUI.buildDialog().setTitle("获取本地化文本")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show();
        });

        builder.addText("读取assets内文件").summary("getAssetsAsStream").onClick((pluginUI, item) -> {
            String text;
            try (InputStream is = context.getAssetsAsStream("strings.mtl")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                text = baos.toString("UTF-8");
            } catch (IOException e) {
                text = e.toString();
            }
            PluginView pluginView = pluginUI.buildVerticalLayout()
                    .addEditText().text(text)
                    .singleLine(false).readOnly()
                    .syntaxHighlight(".mtl")
                    .textSize(13).build();

            pluginUI.buildDialog().setTitle("读取assets内文件")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show();
        });

        builder.addText("打开系统浏览器").summary("openBrowser").onClick((pluginUI, item) -> {
            context.openBrowser("https://mt2.cn");
        });
        builder.addText("打开内置浏览器").summary("openBuiltinBrowser").onClick((pluginUI, item) -> {
            context.openBuiltinBrowser("https://mt2.cn", false);
        });
        builder.addText("剪贴板操作").summary("has/get/setClipboardText").onClick((pluginUI, item) -> {
            PluginView pluginView = pluginUI.buildVerticalLayout().paddingHorizontal(pluginUI.dialogPaddingHorizontal()).paddingTopDp(8)
                    .addButton().widthMatchParent().text("hasClipboardText()").allCaps(false).onClick(view -> {
                        context.showToast(String.valueOf(context.hasClipboardText()));
                    })
                    .addButton().widthMatchParent().text("getClipboardText()").allCaps(false).onClick(view -> {
                        context.showToast(context.getClipboardText());
                    })
                    .addButton().widthMatchParent().text("setClipboardText(\"abc\")").allCaps(false).onClick(view -> {
                        context.setClipboardText("abc");
                    })
                    .build();
            pluginUI.buildDialog()
                    .setTitle("剪贴板操作")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show();

        });
        // PluginContext 和 PluginUI 中均有 toast 方法，两者没有区别，哪个调用方便就用哪个
        builder.addText("Toast消息").summary("showToast").onClick((pluginUI, item) -> {
            PluginView pluginView = pluginUI.buildVerticalLayout().paddingHorizontal(pluginUI.dialogPaddingHorizontal()).paddingTopDp(8)
                    .addButton().widthMatchParent().text("showToast()").allCaps(false).onClick(view -> {
                        context.showToast("{key}");
                    })
                    .addButton().widthMatchParent().text("showToastL()").allCaps(false).onClick(view -> {
                        context.showToastL("{key}=%s\ntime=%d","{key}", System.currentTimeMillis());
                    })
                    .addButton().widthMatchParent().text("cancelToast()").allCaps(false).onClick(view -> {
                        context.cancelToast();
                    })
                    .build();
            pluginUI.buildDialog()
                    .setTitle("Toast消息")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show();
        });
        builder.addText("写出日志").summary("log").onClick((pluginUI, item) -> {
            context.log("这是一条日志");
            context.log("这是一条错误日志", new Exception());
            pluginUI.showToast("日志写出成功");
        });
        builder.addText("查看日志").summary("openLogViewer").onClick((pluginUI, item) -> {
            context.openLogViewer();
        });
    }
}
