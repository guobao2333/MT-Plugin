package bin.mt.plugin.demo.preference;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;

import java.util.Arrays;

import bin.mt.json.JSONObject;
import bin.mt.json.WriterConfig;
import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginCheckBox;
import bin.mt.plugin.api.ui.PluginCompoundButton;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginProgressBar;
import bin.mt.plugin.api.ui.PluginRadioButton;
import bin.mt.plugin.api.ui.PluginRadioGroup;
import bin.mt.plugin.api.ui.PluginSpinner;
import bin.mt.plugin.api.ui.PluginSwitchButton;
import bin.mt.plugin.api.ui.PluginTextView;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.PluginViewGroup;
import bin.mt.plugin.api.ui.builder.PluginButtonBuilder;
import bin.mt.plugin.api.ui.builder.PluginRootLayoutBuilder;
import bin.mt.plugin.api.ui.builder.PluginTextViewBuilder;
import bin.mt.plugin.api.util.Supplier;

public class ExampleUI implements PluginPreference {
    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("UI 组件");

        builder.addHeader("布局");
        addLayout(context, builder);

        builder.addHeader("基本组件");
        addBasic(context, builder);

        builder.addHeader("输入框");
        addEditText(context, builder);

        builder.addHeader("其它");
        addOthers(context, builder);
    }

    private void addLayout(PluginContext context, Builder builder) {
        add(builder, "垂直布局", "从上往下布局", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addTextView().text("第一行")
                .addTextView().text("第二行")
                .addTextView().text("第三行")
                .addTextView().text("第四行")
                .build()
        );

        add(builder, "水平布局", "从左往右布局", pluginUI -> pluginUI
                .buildHorizontalLayout()
                .addTextView().text("第一列")
                .addTextView().text("第二列").textColor(Color.RED)
                .addTextView().text("第三列").textColor(Color.GREEN)
                .addTextView().text("第四列").textColor(Color.BLUE)
                .build()
        );

        add(builder, "对齐方式", "设置线性布局对齐方式和单独指定对齐方式", pluginUI -> pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    @Override
                    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                        super.handleTextView(pluginUI, builder);
                        builder.textColor(0xFF000000).backgroundColor(0xFFAAAAAA);
                    }
                })
                .buildVerticalLayout()
                .gravity(Gravity.CENTER_HORIZONTAL)
                .addTextView().text("第一行")
                .addTextView().text("第二行").layoutGravity(Gravity.START)
                .addTextView().text("第三行")
                .addTextView().text("第四行").layoutGravity(Gravity.END)
                .build()
        );

        add(builder, "组合布局", "垂直布局嵌套水平布局", pluginUI -> pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    @Override
                    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                        super.handleTextView(pluginUI, builder);
                        builder.textGravity(Gravity.CENTER) // 文字局中
                                .width(0).layoutWeight(1) // 均匀分配宽度
                                .paddingDp(16) // 设置内边距
                                .textColor(0xFF000000);
                    }
                })
                .buildVerticalLayout()
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView().text("1-1").backgroundColor(0xFFFF5555)
                        .addTextView().text("1-2").backgroundColor(0xFF55FF55)
                        .addTextView().text("1-3").backgroundColor(0xFF5555FF)
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView().text("2-1").backgroundColor(0xFF55FF55)
                        .addTextView().text("2-2").backgroundColor(0xFF5555FF)
                        .addTextView().text("2-3").backgroundColor(0xFFFF5555)
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView().text("3-1").backgroundColor(0xFF5555FF)
                        .addTextView().text("3-2").backgroundColor(0xFFFF5555)
                        .addTextView().text("3-3").backgroundColor(0xFF55FF55)
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView().text("4-1").backgroundColor(0xFFFF55FF)
                        .addVerticalLayout().width(0).layoutWeight(1).heightMatchParent().children(subBuilder2 -> subBuilder2
                                .addTextView().text("4-2-1").padding(0).widthMatchParent().backgroundColor(0xFFFFFF55)
                                .addTextView().text("4-2-2").padding(0).widthMatchParent().backgroundColor(0xFF55FFFF)
                        )
                )
                .build()
        );

        add(builder, "统一宽度", "让多个 View 保持一样的宽度（以最宽者为准）", pluginUI -> pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    @Override
                    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                        super.handleTextView(pluginUI, builder);
                        builder.textColor(0xFF000000).backgroundColor(0xFFAAAAAA);
                    }
                })
                .buildVerticalLayout()
                .addTextView().text("原本的效果").bold()
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("text1").text("用户名")
                        .addEditText()
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("text2").text("密码")
                        .addEditText()
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("text3").text("电子邮箱")
                        .addEditText()
                )
                .addTextView().text("使用 unifyWidth() 统一宽度的效果").bold()
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("text4").text("用户名")
                        .addEditText()
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("text5").text("密码")
                        .addEditText()
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("text6").text("电子邮箱")
                        .addEditText()
                )
                .unifyWidth("text4", "text5", "text6")
                .build()
        );

        add(builder, "桢布局", "一种将子View叠加显示的布局，可指定对齐方式", pluginUI -> pluginUI
                .buildFrameLayout()
                .addTextView().text("默认").textSize(30)
                .addTextView().text("默认").textColor(pluginUI.colorAccent()).bold()
                .addTextView().text("局中").layoutGravity(Gravity.CENTER).textSize(30)
                .addTextView().text("局中").layoutGravity(Gravity.CENTER).textColor(pluginUI.colorAccent()).bold()
                .addTextView().text("靠右").layoutGravity(Gravity.END).textSize(30)
                .addTextView().text("靠右").layoutGravity(Gravity.END).textColor(pluginUI.colorAccent()).bold()
                .build()
        );

    }

    private void addBasic(PluginContext context, Builder builder) {
        add(builder, "文本视图", "PluginTextView", pluginUI -> {
            PluginView pluginView = pluginUI.buildVerticalLayout()
                    .addTextView().text("① 这是一个文本")
                    .addTextView().text("② 设置了字体颜色和背景色").textColor(Color.WHITE).backgroundColor(Color.BLACK)
                    .addTextView().text("③ 文本右对齐").textGravity(Gravity.END).widthMatchParent().textColor(Color.WHITE).backgroundColor(Color.DKGRAY).marginVerticalDp(5)
                    .addTextView().height(1).widthMatchParent().backgroundColor(pluginUI.colorDivider()) // 分割线
                    .addTextView().text("④ 设置了内边距").paddingDp(24).textColor(0xFF000000).backgroundColor(0xFFAAAAAA)
                    .addTextView().height(1).widthMatchParent().backgroundColor(pluginUI.colorDivider()) // 分割线
                    .addTextView().text("⑤ 设置了外边距").marginDp(24).textColor(0xFF000000).backgroundColor(0xFFAAAAAA)
                    .addTextView().height(1).widthMatchParent().backgroundColor(pluginUI.colorDivider()) // 分割线
                    .addTextView().text("⑥ 字号48").textSize(48)
                    .addTextView().text("⑦ 字号12").textSize(12)
                    .addTextView().text("⑧ 粗斜体").textStyle(true, true)
                    .addTextView("mono").text("⑨ 等宽体 |W|I|").typeface(Typeface.MONOSPACE)
                    .addTextView().text(new SpannableString("⑩ 富文本") {{ // 这种写法会创建匿名内部类，这边只是为了演示方便，正常不推荐
                        // 设置下富文本属性
                        setSpan(new RelativeSizeSpan(2), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        setSpan(new ForegroundColorSpan(pluginUI.colorError()), 3, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        setSpan(new StyleSpan(Typeface.BOLD), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }})
                    .addTextView().text("⑪ 点我试试").paddingVerticalDp(8).background(pluginUI.selectableItemBackground()).onClick(view -> {
                        pluginUI.getContext().setClipboardText(((PluginTextView) view).getText());
                    })
                    .build();
            // 测试下是否支持等宽字体
            Paint paint = new Paint();
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(100);
            int w1 = (int) paint.measureText("i");
            int w2 = (int) paint.measureText("W");
            if (w1 != w2) {
                PluginTextView textView = pluginView.requireViewById("mono");
                textView.append("（您的系统似乎不支持）");
            }
            return pluginView;
        });

        add(builder, "多行文本", "PluginTextView 多行文本相关演示", pluginUI -> pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    int i = 0;

                    @Override
                    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                        super.handleTextView(pluginUI, builder);
                        builder.widthMatchParent().textColor(0xFF000000)
                                .backgroundColor((i++ % 2) == 0 ? 0xFFDDDDDD : 0xFFAAAAAA);
                    }
                })
                .buildVerticalLayout()
                .addTextView().text("这是一个多行文本\n这是一个多行文本\n这是一个多行文本")
                .addTextView().text("限制最多2行\n限制最多2行\n限制最多2行").maxLines(2)
                .addTextView().text("限制最多2行\n且超出部分显示省略号\n限制最多2行").maxLines(2).ellipsize(TextUtils.TruncateAt.END)
                .addTextView().text("设置显示高度为3行").lines(3)
                .addTextView().text("行间距大一点\n行间距大一点\n行间距大一点").lineSpacing(0, 1.5f)
                .addTextView().text("下面演示多行文本在调用 lines(1) 和 singleLine() 时的区别：").textSize(12).paddingVerticalDp(4)
                .addTextView().text("默认情况\n12345678\n12345678")
                .addTextView().text("调用 lines(1)\n12345678\n12345678").lines(1)
                .addTextView().text("调用 singleLine()\n12345678\n12345678").singleLine()
                .build()
        );

        add(builder, "普通按钮", "PluginButton", pluginUI -> pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    @Override
                    protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
                        super.handleButton(pluginUI, builder);
                        builder.widthMatchParent();
                        builder.onClick(view -> context.showToast("点击了 " + ((PluginButton) view).getText()));
                        builder.onLongClick(view -> {
                            context.showToast("长按了 " + ((PluginButton) view).getText());
                            return true;
                        });
                    }
                })
                .buildVerticalLayout()
                .addButton().text("default").style(PluginButton.Style.DEFAULT)
                .addButton().text("filled").style(PluginButton.Style.FILLED)
                .addButton().text("outlined").style(PluginButton.Style.OUTLINED)
                .addTextView().text("关闭按钮的强制大写")
                .addButton().text("default").allCaps(false)
                .build()
        );

        add(builder, "单选按钮", "PluginRadioButton 与 PluginRadioGroup", pluginUI -> {
            PluginView pluginView = pluginUI
                    .buildVerticalLayout()
                    //----------------
                    .addTextView().text("① RadioGroup 使用 position 定位选项")
                    .addRadioGroup("groupPos", true).children(subBuilder -> subBuilder
                            .addRadioButton().text("选项0")
                            .addRadioButton().text("选项1")
                            .addTextView().text(" -------").textSize(12) // 非RadioButton组件不影响position
                            .addRadioButton().text("选项2")
                            .addRadioButton().text("选项3")
                    ).check(1) // 按位置选中选项
                    .postOnCheckedChanged((group, checkedButton, checkedPosition) -> context.showToast("选中了position " + checkedPosition))
                    //----------------
                    .addTextView().text("② RadioGroup 使用 id 定位选项").marginTopDp(8)
                    .addRadioGroup("groupId", false).children(subBuilder -> subBuilder
                            .addRadioButton().id("radio0").text("选项0")
                            .addRadioButton().id("radio1").text("选项1")
                            .addRadioButton().id("radio2").text("选项2")
                            .addRadioButton().id("radio3").text("选项3")
                    ).check("radio1") // 按id选中选项
                    .postOnCheckedChanged((group, checkedButton, checkedPosition) -> context.showToast("选中了id " + (checkedButton == null ? null : checkedButton.getId())))
                    //----------------
                    .addTextView().text("③ 手动实现单选逻辑").marginTopDp(8)
                    .addHorizontalLayout().children(subBuilder -> subBuilder
                            .addRadioButton().id("radio-m0").text("选项0")
                            .addRadioButton().id("radio-m1").text("选项1").check()
                    )
                    .addHorizontalLayout().children(subBuilder -> subBuilder
                            .addRadioButton().id("radio-m2").text("选项2")
                            .addRadioButton().id("radio-m3").text("选项3")
                    )
                    //----------------
                    .addButton("button").text("获取选中位置").widthMatchParent()
                    .build();
            // 手动实现单选逻辑，当RadioGroup无法满足布局需求时使用
            PluginRadioButton[] radioButtons = {
                    pluginView.requireViewById("radio-m0"),
                    pluginView.requireViewById("radio-m1"),
                    pluginView.requireViewById("radio-m2"),
                    pluginView.requireViewById("radio-m3")
            };
            PluginCompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
                if (!isChecked) {
                    return;
                }
                context.showToast("选中了 " + buttonView.getText());
                for (PluginRadioButton radioButton : radioButtons) {
                    if (radioButton != buttonView && radioButton.isChecked()) {
                        radioButton.setChecked(false);
                    }
                }
            };
            for (PluginRadioButton radioButton : radioButtons) {
                radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
            }

            PluginRadioGroup groupPos = pluginView.requireViewById("groupPos");
            PluginRadioGroup groupId = pluginView.requireViewById("groupId");
            pluginView.requireViewById("button").setOnClickListener(view -> {
                StringBuilder sb = new StringBuilder();
                sb.append("① 选中了 ").append(radioButtonToString(groupPos.getCheckedRadioButton())).append("\n");
                sb.append("② 选中了 ").append(radioButtonToString(groupId.getCheckedRadioButton())).append("\n");
                PluginRadioButton checkedButton = null;
                for (PluginRadioButton radioButton : radioButtons) {
                    if (radioButton.isChecked()) {
                        checkedButton = radioButton;
                        break;
                    }
                }
                sb.append("③ 选中了 ").append(radioButtonToString(checkedButton));
                context.showToastL(sb);
            });
            return pluginView;
        });

        add(builder, "开关按钮", "PluginSwitchButton", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addSwitchButton("switch1").text("① 开关1").widthMatchParent()
                .addSwitchButton("switch2").text("② 开关2").widthMatchParent().marginTopDp(12).check()
                .addSwitchButton("switch3").text("③ 事件监听").widthMatchParent().marginTopDp(12).onCheckedChange((buttonView, isChecked) -> context.showToast("isChecked=" + isChecked))
                .addButton("button").text("获取选中位置").widthMatchParent().onClick(view -> {
                    PluginViewGroup rootView = view.getRootView();
                    PluginSwitchButton switch1 = rootView.requireViewById("switch1");
                    PluginSwitchButton switch2 = rootView.requireViewById("switch2");
                    PluginSwitchButton switch3 = rootView.requireViewById("switch3");
                    String sb = "① " + (switch1.isChecked() ? "已选中" : "未选中") + "\n" +
                            "② " + (switch2.isChecked() ? "已选中" : "未选中") + "\n" +
                            "③ " + (switch3.isChecked() ? "已选中" : "未选中");
                    context.showToastL(sb);
                })
                .build()
        );

        add(builder, "多选框", "PluginCheckBox", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addCheckBox("check1").text("① 多选框1").check()
                .addCheckBox("check2").text("② 多选框2")
                .addCheckBox("check3").text("③ 事件监听").onCheckedChange((buttonView, isChecked) -> context.showToast("isChecked=" + isChecked))
                .addButton("button").text("获取选中位置").widthMatchParent()
                .onClick(view -> {
                    PluginViewGroup rootView = view.getRootView();
                    PluginCheckBox check1 = rootView.requireViewById("check1");
                    PluginCheckBox check2 = rootView.requireViewById("check2");
                    PluginCheckBox check3 = rootView.requireViewById("check3");
                    String sb = "① " + (check1.isChecked() ? "已选中" : "未选中") + "\n" +
                            "② " + (check2.isChecked() ? "已选中" : "未选中") + "\n" +
                            "③ " + (check3.isChecked() ? "已选中" : "未选中");
                    context.showToastL(sb);
                })
                .build()
        );

        add(builder, "进度条", "PluginProgressBar", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addTextView().text("确定进度的水平进度条")
                .addProgressBar().progress(75) // 默认最大进度为100
                .addProgressBar().progress(66).secondaryProgress(132).maxProgress(200)
                .addTextView().text("不确定进度的水平进度条")
                .addProgressBar().indeterminate(true)
                .addTextView().text("不确定进度的圆形进度条")
                .addHorizontalLayout().gravity(Gravity.CENTER_VERTICAL).children(subBuilder -> subBuilder
                        .addProgressBar().style(PluginProgressBar.Style.CIRCULAR_SMALL).width(0).layoutWeight(1)
                        .addProgressBar().style(PluginProgressBar.Style.CIRCULAR).width(0).layoutWeight(1)
                        .addProgressBar().style(PluginProgressBar.Style.CIRCULAR_LARGE).width(0).layoutWeight(1)
                )
                .addTextView().text("圆形进度条不支持确定进度").textSize(13).textColor(pluginUI.colorTextSecondary())
                .build()
        );

        add(builder, "下拉选择框", "PluginSpinner", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addSpinner("spinner").widthMatchParent().items(Arrays.asList("选项1", "选项2", "选项3", "选项3"))
                .selection(1)
                // 第一次打开就会弹出Toast，如果不想弹出可改为postOnItemSelected
                .onItemSelected((spinner, position) -> context.showToast("选中了 " + spinner.getItem(position)))
                .build()
        );
    }

    private void addEditText(PluginContext context, Builder builder) {
        PluginUI.Style style = STYLE.new Modifier() {
            boolean first = true;

            @Override
            protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                super.handleTextView(pluginUI, builder);
                builder.textSize(12).textColor(pluginUI.colorTextSecondary());
                if (first) {
                    first = false;
                } else {
                    builder.marginTopDp(6);
                }
            }
        };

        add(builder, "两种风格的输入框", "PluginEditText", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addEditText().text("普通风格输入框 - 默认单行模式")
                .addEditBox().text("Box风格输入框 - 默认多行模式").lines(5)
                .build()
        );

        add(builder, "提示内容", "设置 hint 在编辑框内容为空时指导用户输入", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addEditText().hint("请输入账号")
                .build()
        );

        add(builder, "设置行数", "设置编辑框高度为指定函数", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addTextView().text("手动回车换行试试")
                .addEditBox().marginTopDp(5).hint("无限制")
                .addEditBox().marginTopDp(5).hint("固定5行").lines(5)
                .addEditBox().marginTopDp(5).hint("最多5行").maxLines(5)
                .addEditBox().marginTopDp(5).hint("最少2行最多5行").minLines(2).maxLines(5)
                .build()
        );

        add(builder, "输入类型与内容限制", "设置输入类型、限制输入长度", pluginUI -> pluginUI
                .defaultStyle(style)
                .buildVerticalLayout()
                .addTextView().text("允许换行")
                .addEditText().inputTypeMultiline()
                .addTextView().text("输入数字")
                .addEditText().inputTypeNumber()
                .addTextView().text("输入数字（带正负号）")
                .addEditText().inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .addTextView().text("输入小数")
                .addEditText().inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .addTextView().text("输入密码（目前不支持隐藏密码，后续完善）")
                .addEditText().inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .addTextView().text("最多输入10个字符")
                .addEditText().maxLength(10)
                .addTextView().text("只读模式")
                .addEditText().text("只能看不能改").readOnly()
                .build()
        );

        add(builder, "全选并弹出输入法", "全选文本并在显示对话框时自动弹出输入法", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addEditText().text("abc").selectAll().requestFocusAndShowIME()
                .build()
        );

        add(builder, "自动换行", "三种自动换行模式演示", pluginUI -> pluginUI
                .defaultStyle(style)
                .buildVerticalLayout()
                .addTextView().text("关闭自动换行")
                .addEditText().softWrap(PluginEditText.SOFT_WRAP_DISABLE).text("12345678 12345678 12345678 12345678 12345678")
                .addTextView().text("自动换行-防止断词模式")
                .addEditText().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD).text("12345678 12345678 12345678 12345678 12345678")
                .addTextView().text("自动换行-完全填充模式")
                .addEditText().softWrap(PluginEditText.SOFT_WRAP_COMPLETELY_FILLED).text("12345678 12345678 12345678 12345678 12345678")
                .build()
        );

        add(builder, "内容监听", "监听编辑框文本内容变化", pluginUI -> {
            PluginView pluginView = pluginUI
                    .buildVerticalLayout()
                    .addEditText("edit").hint("请输入数字").requestFocusAndShowIME()
                    .addTextView("error").text("请输入数字！").textColor(Color.RED).gone()
                    .addTextView("msg")
                    .build();
            PluginEditText editText = pluginView.requireViewById("edit");
            PluginTextView error = pluginView.requireViewById("error");
            PluginTextView msg = pluginView.requireViewById("msg");
            editText.addTextChangedListener(new TextWatcher() {
                String delete, insert;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    delete = s.subSequence(start, start + count).toString();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    insert = s.subSequence(start, start + count).toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().matches("\\d*")) {
                        error.setGone();
                    } else {
                        error.setVisible();
                    }
                    if (!delete.isEmpty() || !insert.isEmpty()) {
                        StringBuilder sb = new StringBuilder("您刚刚");
                        if (!delete.isEmpty()) {
                            sb.append("删除了“").append(delete).append("”");
                        }
                        if (!insert.isEmpty()) {
                            sb.append("输入了“").append(insert).append("”");
                        }
                        msg.setText(sb);
                    } else {
                        msg.setText(null);
                    }
                }
            });
            return pluginView;
        });

        add(builder, "单行模式", "关于 singleLine 和 inputType", pluginUI -> pluginUI
                .defaultStyle(style)
                .buildVerticalLayout()
                .addTextView().text("默认就是单行模式")
                .addEditText().text("abc\ndef")
                .addTextView().text("关闭单行模式")
                .addEditText().singleLine(false).text("abc\ndef")
                .addTextView().text("设为多行InputType也会关闭单行模式")
                .addEditText().inputTypeMultiline().text("abc\ndef")
                .addTextView().text("单行模式下开启自动换行，过长的内容仍然会显示到下一行")
                .addEditText().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD).text("abc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc")
                .addTextView().text("上面例子关闭单行模式的效果（主要区别在于换行符）")
                .addEditText().softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD).text("abc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc\nabc")
                .singleLine(false).maxLines(5)
                .build()
        );

        add(builder, "语法高亮", "代码语法高亮效果演示", pluginUI -> pluginUI
                .defaultStyle(style)
                .buildVerticalLayout()
                .addTextView().text("Java")
                .addEditBox().textSize(12).syntaxHighlight("Java").text("public class HelloWorld {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello World!\");\n" +
                        "    }\n" +
                        "}").readOnly()
                .addTextView().text("C")
                .addEditBox().textSize(12).syntaxHighlight("C").text("#include <stdio.h>\n" +
                        "\n" +
                        "int main() {\n" +
                        "    printf(\"Hello, World!\\n\");\n" +
                        "    return 0;\n" +
                        "}").readOnly()
                .addTextView().text("XML")
                // 也可以使用文件后缀名来加载语法
                .addEditBox().textSize(12).syntaxHighlight(".xml").text("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    android:layout_width=\"match_parent\"\n" +
                        "    android:layout_height=\"match_parent\"\n" +
                        "    android:gravity=\"center\">\n" +
                        "\n" +
                        "    <TextView\n" +
                        "        android:layout_width=\"wrap_content\"\n" +
                        "        android:layout_height=\"wrap_content\"\n" +
                        "        android:text=\"Hello World!\" />\n" +
                        "\n" +
                        "</FrameLayout>").readOnly()
                .addTextView().text("Regex 正则搜索")
                .addEditText().syntaxHighlight("Regex").text("[A-Z]{1,50}(_[A-Z]{1,50}){1,50}").readOnly()
                .addTextView().text("RegexReplacement 正则替换")
                .addEditText().syntaxHighlight("Regex").text("a$1b").readOnly()
                .addButton().text("语法开发手册").widthMatchParent().onClick(view -> context.openBuiltinBrowser("https://mt2.cn/guide/file/mt-syntax.html#%E5%B1%9E%E6%80%A7-name", false))
                .build()
        );

        add(builder, "括号对高亮", "括号对高亮效果演示", pluginUI -> pluginUI
                .defaultStyle(style)
                .buildVerticalLayout()
                .addTextView().text("开启括号对高亮")
                .addEditText().text("(abc)").selectEnd().requestFocus()
                .addTextView().text("关闭括号对高亮")
                .addEditText().text("(abc)").selectEnd().disableBracketHighlight()
                .addTextView().text("默认高亮 {} [] ()，具体受到当前语法高亮规则控制")
                .build()
        );
    }

    private static JSONObject data;

    private void addOthers(PluginContext context, Builder builder) {
        add(builder, "内置颜色", "PluginUI提供的一些颜色值", pluginUI -> pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    @Override
                    protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                        super.handleTextView(pluginUI, builder);
                        if (builder.getId() == null) {
                            builder.heightMatchParent().widthMatchParent();
                        } else {
                            builder.paddingRight(8);
                        }
                    }

                    @Override
                    protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
                        super.handleButton(pluginUI, builder);
                        builder.allCaps(false).widthMatchParent();
                    }
                })
                .buildVerticalLayout()
                .addTextView().text("当前是否为深色主题（夜间模式）= " + pluginUI.isDarkTheme())
                // 来一条分割线
                .addTextView().widthMatchParent().height(1).backgroundColor(pluginUI.colorDivider()).marginVertical(12)
                //
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h1").text("colorPrimary")
                        .addTextView().backgroundColor(pluginUI.colorPrimary())
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h2").text("colorAccent")
                        .addTextView().backgroundColor(pluginUI.colorAccent())
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h3").text("colorDivider")
                        .addTextView().backgroundColor(pluginUI.colorDivider())
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h4").text("colorError")
                        .addTextView().backgroundColor(pluginUI.colorError())
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h5").text("colorWarning")
                        .addTextView().backgroundColor(pluginUI.colorWarning())
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h6").text("colorText")
                        .addTextView().backgroundColor(pluginUI.colorText())
                )
                .addHorizontalLayout().children(subBuilder -> subBuilder
                        .addTextView("h7").text("colorTextSecondary")
                        .addTextView().backgroundColor(pluginUI.colorTextSecondary())
                )
                .unifyWidth("h1", "h2", "h3", "h4", "h5", "h6", "h7")
                // 来一条分割线
                .addTextView().widthMatchParent().height(1).backgroundColor(pluginUI.colorDivider()).marginVertical(12)
                //
                .addButton().text("colorTextStateList 启用").textColor(pluginUI.colorTextStateList())
                .addButton().text("colorTextStateList 禁用").textColor(pluginUI.colorTextStateList()).enable(false)
                .addButton().text("colorTextSecondaryStateList 启用").textColor(pluginUI.colorTextSecondaryStateList())
                .addButton().text("colorTextSecondaryStateList 禁用").textColor(pluginUI.colorTextSecondaryStateList()).enable(false)
                .build()
        );

        add(builder, "本地化文本1", "PluginViewBuilder中引用本地化文本", pluginUI -> pluginUI
                .buildVerticalLayout()
                .addTextView().text("请结合代码和界面效果一起看").bold().paddingBottomDp(12)
                // 加载默认语言包中key对应的文本
                .addTextView().text("{key}")
                // 加载example语言包中key对应的文本
                .addTextView().text("{example:key}")
                // 加载默认语言包中key2对应的文本：实际不存在key2，因此直接显示{key2}
                .addTextView().text("{key2}")
                // 如果一定要显示{key}，可传入非String类型文本
                .addTextView().text(new SpannedString("{key}"))
                // 所有的text()和hint()都支持
                .addButton().text("{key}")
                .addCheckBox().text("{key}")
                .addSwitchButton().text("{key}")
                .addRadioButton().text("{key}")
                .addEditText().text("{key}")
                .addEditText().hint("{key}")
                // spinner的items需要手动转一下
                .addSpinner().items(pluginUI.getContext().getStringList("{item0}", "{item1}", "{item2}", "{item3}"))
                .build()
        );

        add(builder, "本地化文本2", "PluginView中引用本地化文本", pluginUI -> {
            PluginView pluginView = pluginUI
                    .buildVerticalLayout()
                    .addTextView().text("请结合代码和界面效果一起看").bold().paddingBottomDp(12)
                    .addTextView("text1")
                    .addTextView("text2")
                    .addTextView("text3")
                    .addTextView("text4")
                    .addButton("button")
                    .addCheckBox("check")
                    .addSwitchButton("switch")
                    .addRadioButton("radio")
                    .addEditText("edit1")
                    .addEditText("edit2")
                    .addSpinner("spinner")
                    .build();
            PluginTextView text1 = pluginView.requireViewById("text1");
            PluginTextView text2 = pluginView.requireViewById("text2");
            PluginTextView text3 = pluginView.requireViewById("text3");
            PluginTextView text4 = pluginView.requireViewById("text4");
            PluginButton button = pluginView.requireViewById("button");
            PluginCheckBox check = pluginView.requireViewById("check");
            PluginSwitchButton switch0 = pluginView.requireViewById("switch");
            PluginRadioButton radio = pluginView.requireViewById("radio");
            PluginEditText edit1 = pluginView.requireViewById("edit1");
            PluginEditText edit2 = pluginView.requireViewById("edit2");
            PluginSpinner spinner = pluginView.requireViewById("spinner");

            // 加载默认语言包中key对应的文本
            text1.setText("{key}");
            // 加载example语言包中key对应的文本
            text2.setText("{example:key}");
            // 加载默认语言包中key2对应的文本：实际不存在key2，因此直接显示{key2}
            text3.setText("{key2}");
            // 如果一定要显示{key}，可传入非String类型文本
            text4.setText(new SpannedString("{key}"));
            // 所有的text()和hint()都支持
            button.setText("{key}");
            check.setText("{key}");
            switch0.setText("{key}");
            radio.setText("{key}");
            edit1.setText("{key}");
            edit2.setHint("{key}");
            // spinner的items需要手动转一下
            spinner.setItems(pluginUI.getContext().getStringList("{item0}", "{item1}", "{item2}", "{item3}"));
            return pluginView;
        });

        add(builder, "JSON 数据转换", "快速将 JSON 数据赋值给 UI 组件以及保存回 JSON", pluginUI -> {
            PluginView pluginView = pluginUI
                    .defaultStyle(STYLE.new Modifier() {
                        @Override
                        protected void handleTextView(PluginUI pluginUI, PluginTextViewBuilder builder) {
                            super.handleTextView(pluginUI, builder);
                            builder.textSize(12).textColor(pluginUI.colorTextSecondary()).marginTopDp(6);
                        }
                    })
                    .disableStrictIdMode()
                    .buildVerticalLayout()
                    // 无默认值
                    .addTextView().text("无默认值").textSize(16).bold().textColor(pluginUI.colorText())
                    // EditText
                    .addTextView().text("EditText.text")
                    .addEditText("text1").text(data)
                    // CheckBox
                    .addTextView().text("CheckBox.checked")
                    .addCheckBox("check1").checked(data)
                    // SwitchButton
                    .addTextView().text("SwitchButton.checked")
                    .addSwitchButton("switch1").checked(data)
                    // Spinner
                    .addTextView().text("Spinner.selection")
                    .addSpinner("spinner1").items(Arrays.asList("项目0", "项目1", "项目2", "项目3", "项目4")).selection(data)
                    // RadioGroup id
                    .addTextView().text("RadioGroup.checkedId")
                    .addRadioGroup("radioId1", false).children(subBuilder -> subBuilder
                            .addRadioButton("id1").text("#1")
                            .addRadioButton("id2").text("#2")
                    ).checkedId(data)
                    // RadioGroup position
                    .addTextView().text("RadioGroup.checkedPosition")
                    .addRadioGroup("radioPos1", false).children(subBuilder -> subBuilder
                            .addRadioButton().text("#1")
                            .addRadioButton().text("#2")
                    ).checkedPosition(data)
                    // 有默认值
                    .addTextView().text("有默认值").textSize(16).bold().textColor(pluginUI.colorText())
                    // EditText
                    .addTextView().text("EditText.text 带有默认值")
                    .addEditText("text2").text(data, "带有默认值")
                    // CheckBox
                    .addTextView().text("CheckBox.checked 默认true")
                    .addCheckBox("check2").checked(data, true)
                    // SwitchButton
                    .addTextView().text("SwitchButton.checked 默认true")
                    .addSwitchButton("switch2").checked(data, true)
                    .addTextView().text("Spinner.selection 默认2")
                    // Spinner
                    .addSpinner("spinner2").items(Arrays.asList("项目0", "项目1", "项目2", "项目3", "项目4")).selection(data, 2)
                    // RadioGroup id
                    .addTextView().text("RadioGroup.checkedId 默认id1")
                    .addRadioGroup("radioId2", false).children(subBuilder -> subBuilder
                            .addRadioButton("id1").text("#1")
                            .addRadioButton("id2").text("#2")
                    ).checkedId(data, "id1")
                    // RadioGroup position
                    .addTextView().text("RadioGroup.checkedPosition 默认位置0")
                    .addRadioGroup("radioPos2", false).children(subBuilder -> subBuilder
                            .addRadioButton().text("#1")
                            .addRadioButton().text("#2")
                    ).checkedPosition(data, 0)
                    // data
                    .addButton("button1").text("查看数据").widthMatchParent()
                    .addButton("button2").text("保存数据").widthMatchParent()
                    .addButton("button3").text("清除数据").widthMatchParent()
                    .build();
            PluginEditText text1 = pluginView.requireViewById("text1");
            PluginEditText text2 = pluginView.requireViewById("text2");
            PluginCheckBox check1 = pluginView.requireViewById("check1");
            PluginCheckBox check2 = pluginView.requireViewById("check2");
            PluginSwitchButton switch1 = pluginView.requireViewById("switch1");
            PluginSwitchButton switch2 = pluginView.requireViewById("switch2");
            PluginSpinner spinner1 = pluginView.requireViewById("spinner1");
            PluginSpinner spinner2 = pluginView.requireViewById("spinner2");
            PluginRadioGroup radioId1 = pluginView.requireViewById("radioId1");
            PluginRadioGroup radioId2 = pluginView.requireViewById("radioId2");
            PluginRadioGroup radioPos1 = pluginView.requireViewById("radioPos1");
            PluginRadioGroup radioPos2 = pluginView.requireViewById("radioPos2");
            Supplier<JSONObject> jsonDataSupplier = () -> new JSONObject()
                    .putText(text1)
                    .putText(text2)
                    .putChecked(check1)
                    .putChecked(check2)
                    .putChecked(switch1)
                    .putChecked(switch2)
                    .putSelection(spinner1)
                    .putSelection(spinner2)
                    .putCheckedId(radioId1)
                    .putCheckedId(radioId2)
                    .putCheckedPosition(radioPos1)
                    .putCheckedPosition(radioPos2);
            // 查看数据
            pluginView.requireViewById("button1").setOnClickListener(view -> {
                String savedData = data == null ? "" : data.toString(WriterConfig.PRETTY_PRINT);
                String currentData = jsonDataSupplier.get().toString(WriterConfig.PRETTY_PRINT);
                PluginView jsonView = pluginUI.buildVerticalLayout()
                        .addTextView().text("已保存").textSize(14).bold()
                        .addEditBox().text(savedData).textSize(13).syntaxHighlight("JSON")
                        .addTextView().text("未保存").textSize(14).bold()
                        .addEditBox().text(currentData).textSize(13).syntaxHighlight("JSON")
                        .build();
                pluginUI.buildDialog().setView(jsonView).show();
            });
            // 保存数据
            pluginView.requireViewById("button2").setOnClickListener(view -> {
                ExampleUI.data = jsonDataSupplier.get();
                context.showToast("保存成功，请关闭对话框再打开查看效果");
            });
            // 清除数据
            pluginView.requireViewById("button3").setOnClickListener(view -> {
                ExampleUI.data = null;
                context.showToast("清除成功，请关闭对话框再打开查看效果");
            });
            return pluginView;
        });
    }

    private static String radioButtonToString(PluginRadioButton button) {
        return button == null ? "null" : button.getText().toString();
    }

    private static final PluginUI.Style STYLE = PluginUI.DEFAULT_STYLE.new Modifier() {
        @Override
        protected void handleRootLayout(PluginUI pluginUI, PluginRootLayoutBuilder builder) {
            super.handleRootLayout(pluginUI, builder);
            // 这里的演示对话框没有标题也没有按钮，需要添加下上下内边距
            builder.paddingVertical(pluginUI.dialogPaddingVertical());
        }
    };

    private static void add(Builder preferenceBuilder, String title, String summary, ViewBuilder viewBuilder) {
        preferenceBuilder.addText(title).summary(summary).onClick((pluginUI, item) -> {
            pluginUI.defaultStyle(STYLE);
            PluginView view = viewBuilder.buildView(pluginUI);
            pluginUI.buildDialog().setView(view).show();
        });
    }

    private interface ViewBuilder {

        PluginView buildView(PluginUI pluginUI);

    }
}
