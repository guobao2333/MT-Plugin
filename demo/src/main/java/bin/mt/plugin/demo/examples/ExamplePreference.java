package bin.mt.plugin.demo.examples;

import android.content.SharedPreferences;

import java.util.concurrent.atomic.AtomicInteger;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.util.Supplier;

public class ExamplePreference implements PluginPreference {

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("设置界面").subtitle("副标题");

        builder.addHeader("通用设置项");

        builder.addText("纯文本")
                .summary("单纯用来显示文字");

        builder.addText("链接文本")
                .summary("除了显示文字，点击还能打开网址")
                .url("https://bbs.binmt.cc");

        builder.addInput("选项-输入内容", "key_input")
                .summary("请输入内容")
                .hint("提示内容")
                .valueAsSummary()
                .defaultValue("默认值");

        builder.addList("选项-单选列表", "key_list")
                .summary("未选中任何项目")
                // .defaultValue("1")
                .addItem("项目1", "1").summary("选中了选项1")
                .addItem("项目2", "2").summary("选中了选项2");

        builder.addSwitch("选项-开关", "key_switch")
                .defaultValue(true)
                .summaryOn("开")
                .summaryOff("关");


        builder.addHeader("自定义点击事件", "custom_header");

        // 自定义点击
        AtomicInteger count = new AtomicInteger();
        builder.addText("自定义点击", "custom_click")
                .summary("点击了 0 次")
                .onClick((pluginUI, item) -> item.setSummary("点击了 " + count.incrementAndGet() + " 次"));


        // 自定义对话框
        SharedPreferences preferences = context.getPreferences();
        Supplier<String> summarySupplier = () -> {
            String value = preferences.getString("custom_input", null);
            return value == null || value.isEmpty() ? "点击试试" : "您输入了：" + value;
        };
        builder.addText("自定义对话框", "custom_input")
                .summary(summarySupplier.get())
                .onClick((pluginUI, item) -> {
                    // 创建一个输入框
                    PluginView view = pluginUI.buildVerticalLayout()
                            .addEditText("input").text(preferences.getString(item.getKey(), null)).selectAll()
                            .build();
                    PluginEditText input = view.requireViewById("input");

                    // 获取焦点并弹出输入法
                    input.requestFocusAndShowIME();

                    // 创建对话框并显示
                    pluginUI.buildDialog()
                            .setTitle("自定义对话框")
                            .setView(view)
                            .setPositiveButton("确定", (dialog, which) -> {
                                // 保存输入内容
                                String text = input.getText().toString();
                                preferences.edit().putString(item.getKey(), text).apply();
                                // 更新summary
                                item.setSummary(summarySupplier.get());
                            })
                            .setNegativeButton("取消", null)
                            .show();
                });

        // 监听选项变化
        builder.addHeader("监听选项变化");
        builder.addSwitch("启用自定义分组", "enable_custom")
                .summary("点击看看上面有什么变化")
                .defaultValue(true);
        builder.addInput("输入内容", "listen_input")
                .summary("输入内容变化后会弹出Toast");
        // 监听用户改变选项事件
        builder.onPreferenceChange((pluginUI, preferenceItem, newValue) -> {
            PreferenceScreen preferenceScreen = preferenceItem.getPreferenceScreen();
            switch (preferenceItem.getKey()) {
                case "enable_custom" -> {
                    boolean enable = (boolean) newValue;
                    preferenceScreen.requireHeader("custom_header").setEnabled(enable);
                    preferenceScreen.requirePreference("custom_click").setEnabled(enable);
                    preferenceScreen.requirePreference("custom_input").setEnabled(enable);
                }
                case "listen_input" -> {
                    pluginUI.showToast("输入内容：" + newValue);
                }
            }
        });
        // 可以在上面创建自定义分组时调用enable()方法设置启用状态
        // 为方便演示这里放在创建完毕事件里统一设置
        builder.onCreated((pluginUI, preferenceScreen) -> {
            boolean enable = preferences.getBoolean("custom_header", true);
            preferenceScreen.requireHeader("custom_header").setEnabled(enable);
            preferenceScreen.requirePreference("custom_click").setEnabled(enable);
            preferenceScreen.requirePreference("custom_input").setEnabled(enable);
        });

        // 拦截点击事件
        builder.addHeader("拦截点击事件");
        builder.addSwitch("开启拦截", "enable_intercept")
                .summary("开启后点击下面的开关试试")
                .defaultValue(true);
        builder.addSwitch("我被拦截了吗", "be_intercepted")
                .summary("T T")
                .interceptClick((pluginUI, item) -> {
                    if (preferences.getBoolean("enable_intercept", true)) {
                        pluginUI.showToast("我被拦截了");
                        return true;
                    } else {
                        pluginUI.showToast("我没有被拦截");
                        return false;
                    }
                });
        builder.addSwitch("危险选项", "dangerous_option")
                .summary("更改此选项前需要手动确认")
                .interceptClick((pluginUI, item) -> {
                    pluginUI.buildDialog()
                            .setTitle("警告")
                            .setMessage("确定要修改此选项吗？")
                            .setPositiveButton("{ok}", (dialog, which) -> {
                                // 用户确认后，手动切换开关状态
                                SharedPreferences prefs = context.getPreferences();
                                boolean current = prefs.getBoolean("dangerous_option", false);
                                prefs.edit().putBoolean("dangerous_option", !current).apply();
                                // 刷新界面
                                item.getPreferenceScreen().recreate();
                            })
                            .setNegativeButton("{cancel}", null)
                            .show();
                    return true; // 拦截默认行为
                });

    }

}