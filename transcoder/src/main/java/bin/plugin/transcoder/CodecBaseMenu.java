package bin.plugin.transcoder;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorBaseMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginEditTextWatcher;
import bin.mt.plugin.api.ui.PluginSpinner;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.builder.PluginButtonBuilder;
import bin.mt.plugin.api.ui.builder.PluginEditTextBuilder;
import bin.mt.plugin.api.ui.dialog.PluginDialog;
import bin.plugin.transcoder.codec.Base32Codec;
import bin.plugin.transcoder.codec.Base64Codec;
import bin.plugin.transcoder.codec.HexCodec;
import bin.plugin.transcoder.codec.UnicodeCodec;
import bin.plugin.transcoder.codec.UrlCodec;

public class CodecBaseMenu extends BaseTextEditorBaseMenu {
    private static final String KEY_CODEC_SELECTED = "codecSelected";

    @NonNull
    @Override
    public String name() {
        return "{function_name}";
    }

    @NonNull
    @Override
    public Drawable icon() {
        return MaterialIcons.get("code");
    }

    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        return true;
    }

    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        int selStart = editor.getSelectionStart();
        int selEnd = editor.getSelectionEnd();
        String selectedText = editor.subText(selStart, selEnd);
        List<Codec> codecList = Arrays.asList(
                new Base64Codec(pluginUI),
                new Base32Codec(pluginUI),
                new UrlCodec(pluginUI),
                new HexCodec(pluginUI),
                new UnicodeCodec(pluginUI)
        );

        SharedPreferences preferences = pluginUI.getContext().getPreferences();
        String codecSelected = preferences.getString(KEY_CODEC_SELECTED, "");
        int codecIndex = 0;
        if (!codecSelected.isEmpty()) {
            for (int i = 0, size = codecList.size(); i < size; i++) {
                Codec codec = codecList.get(i);
                if (codecSelected.equals(codec.toString())) {
                    codecIndex = i;
                    break;
                }
            }
        }
        int finalCodecIndex = codecIndex;

        PluginEditTextBuilder builder = pluginUI
                .defaultStyle(new PluginUI.StyleWrapper() {
                    @Override
                    protected void handleEditText(PluginUI pluginUI, PluginEditTextBuilder builder) {
                        super.handleEditText(pluginUI, builder);
                        // 统一设置输入框样式
                        builder.minLines(5).maxLines(10).textSize(12).softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD);
                    }

                    @Override
                    protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
                        super.handleButton(pluginUI, builder);
                        // 统一设置按钮样式
                        builder.style(PluginButton.Style.FILLED);
                        builder.text("{button_" + builder.getId() + "}"); // 使用id设置按钮标题
                    }
                })
                .buildVerticalLayout()
                .paddingTop(pluginUI.dialogPaddingVertical() / 2)
                // 标题栏
                .addHorizontalLayout().gravity(Gravity.CENTER_VERTICAL).children(subBuilder -> subBuilder
                        // 标题
                        .addTextView().text(name()).textSize(20).typeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)).width(1).layoutWeight(1)
                        // 选择编码器
                        .addSpinner("codec").items(codecList).selection(finalCodecIndex)
                ).paddingBottomDp(6)
                // 添加输入框1
                .addEditBox("input1").text(selectedText).hint(selectedText.isEmpty() ? "{not_selected_hint}" : "")
                // 添加编码解码按钮
                .addHorizontalLayout().gravity(Gravity.CENTER_VERTICAL).children(layout -> layout
                        .addButton("encode").width(0).layoutWeight(1)
                        .addButton("decode").width(0).layoutWeight(1)
                )
                // 添加输入框2
                .addEditBox("input2");
        if (!selectedText.isEmpty()) {
            // 添加替换原文按钮
            builder.addButton("replace").widthMatchParent().enable(false);
        }
        PluginView view = builder.build();
        PluginDialog dialog = pluginUI.buildDialog()
                .setView(view)
                .setPositiveButton("{close}", null) // {close} 来自MT内置语言包: MT.apk/assets/strings.mtl
                .setNegativeButton("{exchange}", null)
                .setNeutralButton("{options}", null)
                .show();
        PluginSpinner spinner = view.requireViewById("codec");
        PluginEditText input1 = view.requireViewById("input1");
        PluginEditText input2 = view.requireViewById("input2");

        // 选项按钮
        PluginButton neutralButton = dialog.getNeutralButton();

        // 监听编码器切换事件
        spinner.setOnItemSelectedListener((sp, position) -> {
            Codec codec = codecList.get(position);
            PluginView.OnClickListener listener = codec.getOnOptionsButtonClickListener(pluginUI);
            neutralButton.setEnabled(listener != null);
            neutralButton.setOnClickListener(listener);
            preferences.edit().putString(KEY_CODEC_SELECTED, codec.toString()).apply();
        });

        // 编码
        view.requireViewById("encode").setOnClickListener(button -> {
            Codec codec = codecList.get(spinner.getSelection());
            String text = input1.getText().toString();
            try {
                input2.setText(codec.encode(text));
            } catch (Exception e) {
                pluginUI.showToast(e.toString());
            }
        });

        // 解码
        view.requireViewById("decode").setOnClickListener(button -> {
            Codec codec = codecList.get(spinner.getSelection());
            String text = input1.getText().toString();
            try {
                input2.setText(codec.decode(text));
            } catch (Exception e) {
                pluginUI.showToast(e.toString());
            }
        });

        // 交换
        dialog.getNegativeButton().setOnClickListener(button -> {
            String text = input1.getText().toString();
            input1.setText(input2.getText());
            input2.setText(text);
        });

        if (!selectedText.isEmpty()) {
            PluginView replaceButton = view.requireViewById("replace");
            // 第二个输入框内容不为空时才可点击替换原文按钮
            input2.addTextChangedListener(new PluginEditTextWatcher.Simple() {
                @Override
                public void afterTextChanged(PluginEditText editText, Editable s) {
                    replaceButton.setEnabled(!TextUtils.isEmpty(s));
                }
            });
            // 设置替换原文按钮点击事件
            replaceButton.setOnClickListener(button -> {
                editor.replaceText(selStart, selEnd, input2.getText());
                dialog.dismiss();
            });
        }

    }
}
