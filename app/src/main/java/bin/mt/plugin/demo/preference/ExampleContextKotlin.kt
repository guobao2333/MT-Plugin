package bin.mt.plugin.demo.preference

import bin.mt.plugin.api.PluginContext
import bin.mt.plugin.api.preference.PluginPreference
import bin.mt.plugin.api.preference.PluginPreference.TextItem
import bin.mt.plugin.api.ui.PluginUI
import bin.mt.plugin.api.ui.PluginView
import java.io.IOException
import java.nio.charset.StandardCharsets

class ExampleContextKotlin : PluginPreference {
    override fun onBuild(context: PluginContext, builder: PluginPreference.Builder) {
        builder.addText("基本信息（Kotlin）").summary("PluginContext")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                pluginUI.showMessage(
                    "基本信息",
                    "SDKVersion: ${PluginContext.SDK_VERSION}\n" +
                            "PluginID: ${context.pluginId}\n" +
                            "VersionCode: ${context.pluginVersionCode}\n" +
                            "VersionName: ${context.pluginVersionName}\n" +
                            "Locale: ${context.languageCountry}"
                )
            }
        builder.addText("获取本地化文本").summary("getString")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                var text = "getString('{key}') = '${context.getString("{key}")}'\n" +
                        "getString('key') = '${context.getString("key")}'\n" +
                        "getString('{example:key}') = '${context.getString("{example:key}")}'\n"
                text = text.replace("'", "\"")

                val pluginView = pluginUI.buildVerticalLayout()
                    .addEditText().text(text)
                    .singleLine(false).readOnly()
                    .syntaxHighlight("Java")
                    .textSize(13f).build()
                pluginUI.buildDialog().setTitle("获取本地化文本")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show()
            }

        builder.addText("读取assets内文件").summary("getAssetsAsStream")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                var text: String
                try {
                    context.getAssetsAsStream("strings.mtl").use { input ->
                        text = input.readBytes().toString(StandardCharsets.UTF_8)
                    }
                } catch (e: IOException) {
                    text = e.toString()
                }
                val pluginView = pluginUI.buildVerticalLayout()
                    .addEditText().text(text)
                    .singleLine(false).readOnly()
                    .syntaxHighlight(".mtl")
                    .textSize(13f).build()
                pluginUI.buildDialog()
                    .setTitle("读取assets内文件")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show()
            }

        builder.addText("打开系统浏览器").summary("openBrowser")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                context.openBrowser("https://mt2.cn")
            }
        builder.addText("打开内置浏览器").summary("openBuiltinBrowser")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                context.openBuiltinBrowser("https://mt2.cn", false)
            }
        builder.addText("剪贴板操作").summary("has/get/setClipboardText")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                val pluginView = pluginUI.buildVerticalLayout()
                    .paddingHorizontal(pluginUI.dialogPaddingHorizontal())
                    .paddingTopDp(8f)
                    .addButton().widthMatchParent().text("hasClipboardText()").allCaps(false)
                    .onClick { view: PluginView ->
                        context.showToast(context.hasClipboardText().toString())
                    }
                    .addButton().widthMatchParent().text("getClipboardText()").allCaps(false)
                    .onClick { view: PluginView ->
                        context.showToast(context.clipboardText)
                    }
                    .addButton().widthMatchParent().text("setClipboardText(\"abc\")").allCaps(false)
                    .onClick { view: PluginView ->
                        context.clipboardText = "abc"
                    }
                    .build()
                pluginUI.buildDialog()
                    .setTitle("剪贴板操作")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show()
            }
        // PluginContext 和 PluginUI 中均有 toast 方法，两者没有区别，哪个调用方便就用哪个
        builder.addText("Toast消息").summary("showToast")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                val pluginView = pluginUI.buildVerticalLayout().paddingHorizontal(
                    pluginUI.dialogPaddingHorizontal()
                ).paddingTopDp(8f)
                    .addButton().widthMatchParent().text("showToast()").allCaps(false)
                    .onClick { view: PluginView ->
                        context.showToast("{key}")
                    }
                    .addButton().widthMatchParent().text("showToastL()").allCaps(false)
                    .onClick { view: PluginView ->
                        context.showToastL("{key}=%s\ntime=%d", "{key}", System.currentTimeMillis())
                    }
                    .addButton().widthMatchParent().text("cancelToast()").allCaps(false)
                    .onClick { view: PluginView ->
                        context.cancelToast()
                    }
                    .build()
                pluginUI.buildDialog()
                    .setTitle("Toast消息")
                    .setView(pluginView)
                    .setPositiveButton("{close}", null)
                    .show()
            }
        builder.addText("写出日志").summary("log")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                context.log("这是一条日志")
                context.log("这是一条错误日志", Exception())
                pluginUI.showToast("日志写出成功")
            }
        builder.addText("查看日志").summary("openLogViewer")
            .onClick { pluginUI: PluginUI, item: TextItem ->
                context.openLogViewer()
            }
    }
}
