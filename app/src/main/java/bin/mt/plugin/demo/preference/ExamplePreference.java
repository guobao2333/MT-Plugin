package bin.mt.plugin.demo.preference;

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


        builder.addHeader("自定义点击事件");

        // 自定义点击
        AtomicInteger count = new AtomicInteger();
        builder.addText("自定义点击")
                .summary("点击了 0 次")
                .onClick((pluginUI, item) -> item.setSummary("点击了 " + count.incrementAndGet() + " 次"));


        // 自定义对话框
        SharedPreferences preferences = context.getPreferences();
        Supplier<String> summarySupplier = () -> {
            String value = preferences.getString("custom", null);
            return value == null || value.isEmpty() ? "点击试试" : "您输入了：" + value;
        };
        builder.addText("自定义对话框")
                .summary(summarySupplier.get())
                .onClick((pluginUI, item) -> {
                    // 创建一个输入框
                    PluginView view = pluginUI.buildVerticalLayout()
                            .addEditText("input").text(preferences.getString("custom", null)).selectAll()
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
                                preferences.edit().putString("custom", text).apply();
                                // 更新summary
                                item.setSummary(summarySupplier.get());
                            })
                            .setNegativeButton("取消", null)
                            .show();
                });
    }

}