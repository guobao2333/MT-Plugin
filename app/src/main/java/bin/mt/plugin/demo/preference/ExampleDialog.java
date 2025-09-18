package bin.mt.plugin.demo.preference;

import android.os.SystemClock;
import android.view.KeyEvent;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.dialog.DualProgressDialog;
import bin.mt.plugin.api.ui.dialog.LoadingDialog;
import bin.mt.plugin.api.ui.dialog.PluginDialog;
import bin.mt.plugin.api.ui.dialog.ProgressDialog;

public class ExampleDialog implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.addHeader("基本用法");

        builder.title("对话框");

        CharSequence[] items = {"项目0", "项目1", "项目2", "项目3"};

        builder.addText("文字消息").summary("setMessage").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("文字消息")
                .setMessage("消息内容")
                .setPositiveButton("{close}", null)
                .show()
        );

        builder.addText("列表项目").summary("setItems").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("列表项目")
                .setItems(items, (dialog, which) -> {
                    // 点击后自动关闭dialog
                    context.showToast("点击了" + items[which]);
                })
                .show()
        );

        int[] selection = {1};
        builder.addText("单选列表").summary("setSingleChoiceItems").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setSingleChoiceItems(items, selection[0], (dialog, which) -> {
                    // 点击后不会自动关闭dialog
                    selection[0] = which; // 记录选中位置
                })
                .setPositiveButton("{ok}", (dialog, which) -> {
                    context.showToast("选中了" + items[selection[0]]);
                })
                .show()
        );

        boolean[] checked = {false, true, false, true};
        builder.addText("多选列表").summary("setMultiChoiceItems").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
                    // 点击后不会自动关闭dialog
                    checked[which] = isChecked; // 记录选中数据
                })
                .setPositiveButton("{ok}", (dialog, which) -> {
                    StringBuilder sb = new StringBuilder("选中了:");
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            sb.append(' ').append(items[i]);
                        }
                    }
                    context.showToast(sb);
                })
                .show()
        );

        builder.addText("自定义View").summary("setView").onClick((pluginUI, item) -> {
            PluginView pluginView = pluginUI.buildVerticalLayout().addEditText("input").build();
            PluginEditText input = pluginView.requireViewById("input");
            input.requestFocusAndShowIME(); // 加上这句才会弹出输入法
            pluginUI.buildDialog()
                    .setTitle("自定义View")
                    .setView(pluginView)
                    .setPositiveButton("{ok}", (dialog, which) -> {
                        String text = input.getText().toString();
                        context.showToast("输入了: " + text);
                    })
                    .show();
        });

        builder.addText("对话框按钮1").summary("setPositiveButton、setNegativeButton、setNeutralButton").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("对话框按钮1")
                .setMessage("对话框一共可以设置3个按钮，点击后对话框会自动消失")
                .setPositiveButton("按钮1", (dialog, which) -> {
                    // which == PluginDialog.BUTTON_POSITIVE
                    context.showToast("点击了按钮1");
                })
                .setNegativeButton("按钮2", (dialog, which) -> {
                    // which == PluginDialog.BUTTON_NEGATIVE
                    context.showToast("点击了按钮2");
                })
                .setNeutralButton("按钮3", (dialog, which) -> {
                    // which == PluginDialog.BUTTON_NEUTRAL
                    context.showToast("点击了按钮3");
                })
                .show()
        );

        builder.addText("对话框按钮2").summary("实现点击按钮后对话框不会消失").onClick((pluginUI, item) -> {
            PluginDialog dialog = pluginUI.buildDialog()
                    .setTitle("对话框按钮2")
                    .setMessage("在对话框 show() 之后，调用相对应的 getButton() 方法获取按钮的 PluginButton 实例，再覆盖点击事件，这样点击之后对话框就不会消失了")
                    .setPositiveButton("点我", null)
                    .show();
            int[] count = {0};
            dialog.getPositiveButton().setOnClickListener(view -> {
                context.showToast("点击10次后对话框消失 [" + (++count[0]) + "]");
                if (count[0] == 10) {
                    dialog.dismiss();
                }
            });
        });

        builder.addText("对话框不可取消").summary("setCancelable(false)").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("对话框不可取消")
                .setMessage("点击对话框外部或按下返回键，对话框不会消失")
                .setCancelable(false)
                .setPositiveButton("{close}", null)
                .show()
        );

        builder.addText("显示事件监听器").summary("setOnShowListener").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("显示事件监听器")
                .setMessage("在对话框显示时调用")
                .setOnShowListener(dialog -> context.showToast("对话框显示"))
                .setPositiveButton("{close}", null)
                .show()
        );

        builder.addText("消失事件监听器").summary("setOnDismissListener").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("消失事件监听器")
                .setMessage("在对话框消失时调用")
                .setOnDismissListener(dialog -> context.showToast("对话框消失"))
                .setPositiveButton("{close}", null)
                .show()
        );

        builder.addText("取消事件监听器").summary("setOnCancelListener").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("取消事件监听器")
                .setMessage("在对话框取消时调用（点击对话框外部或按下返回键）")
                .setOnCancelListener(dialog -> context.showToast("对话框取消"))
                .setPositiveButton("{close}", null)
                .show()
        );

        builder.addText("按键事件监听器").summary("setOnKeyListener").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("按键事件监听器")
                .setMessage("监听按键事件，例如实现对话框点击外部时不取消，按返回键时取消")
                .setCancelable(false) // 先设置不可取消
                .setOnKeyListener((dialog, keyCode, event) -> {
                    // 按下返回键时手动取消
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                        context.showToast("按下了返回键");
                        dialog.cancel();
                        return true; // 返回true表示事件已经被处理了，无需继续向后传递
                    }
                    return false;
                })
                .setPositiveButton("{close}", null)
                .show()
        );

        // MT内置了一些对话框常用的标题和按钮文本，具体请查看MT安装包/assets/strings语言包
        builder.addText("本地化文本").summary("使用 {key} 格式加载本地化文本").onClick((pluginUI, item) -> pluginUI
                .buildDialog()
                .setTitle("{key}")
                .setMessage("{example:key}")
                .setPositiveButton("{item1}", null)
                .setNegativeButton("{item2}", null)
                .setNeutralButton("{item3}", null)
                .show()
        );

        builder.addHeader("通用封装");

        builder.addText("加载对话框").summary("LoadingDialog").onClick((pluginUI, item) -> {
            LoadingDialog loadingDialog = new LoadingDialog(pluginUI)
                    .setMessage("{processing}")
                    .setSecondaryMessage("10 秒后消失，或者按返回键取消")
                    .setCancelable()
                    .setOnCancelListener(dialog -> {
                        pluginUI.showToast("已取消");
                        dialog.dismiss();
                    }).show();
            new Thread(() -> {
                for (int i = 9; i >= 0; i--) {
                    SystemClock.sleep(1000);
                    loadingDialog.setSecondaryMessage(i + " 秒后消失，或者按返回键取消");
                    if (loadingDialog.isCanceled()) {
                        return;
                    }
                }
                pluginUI.showToast("处理完成");
                loadingDialog.dismiss();
            }).start();
        });

        builder.addText("进度对话框").summary("ProgressDialog").onClick((pluginUI, item) -> {
            ProgressDialog progressDialog = new ProgressDialog(pluginUI)
                    .setTitle("{processing}")
                    .setMessage("10 秒后消失，或者按返回键取消")
                    .setCancelable()
                    .setOnCancelListener(dialog -> {
                        pluginUI.showToast("已取消");
                        dialog.dismiss();
                    }).show();
            new Thread(() -> {
                for (int i = 1; i <= 80; i++) {
                    progressDialog.setProgress(i * 100 / 80);
                    SystemClock.sleep(100);
                    if (progressDialog.isCanceled()) {
                        return;
                    }
                }
                // 最后2秒改为不确定进度模式
                progressDialog.setIndeterminate();
                SystemClock.sleep(2000);
                if (progressDialog.isCanceled()) {
                    return;
                }
                pluginUI.showToast("处理完成");
                progressDialog.dismiss();
            }).start();
        });

        builder.addText("双进度对话框").summary("DualProgressDialog").onClick((pluginUI, item) -> {
            DualProgressDialog progressDialog = new DualProgressDialog(pluginUI)
                    .setTitle("{processing}")
                    .setMessage("10 秒后消失，或者按返回键取消")
                    .setCancelable()
                    .setOnCancelListener(dialog -> {
                        pluginUI.showToast("已取消");
                        dialog.dismiss();
                    }).show();
            new Thread(() -> {
                for (int i = 0; i < 4; i++) {
                    for (int j = 1; j <= 100; j++) {
                        progressDialog.setSubProgress(j);
                        progressDialog.setTotalProgress(i * 25 + j / 4);
                        SystemClock.sleep(25);
                        if (progressDialog.isCanceled()) {
                            return;
                        }
                    }
                }
                pluginUI.showToast("处理完成");
                progressDialog.dismiss();
            }).start();
        });
    }
}
