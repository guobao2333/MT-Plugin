package bin.mt.plugin.demo;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;

import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorToolMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginButton;
import bin.mt.plugin.api.ui.PluginEditText;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.builder.PluginButtonBuilder;
import bin.mt.plugin.api.ui.builder.PluginEditTextBuilder;
import bin.mt.plugin.api.ui.dialog.PluginDialog;
import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.mt.plugin.api.ui.menu.PluginSubMenu;

public class TextEditorToolMenuDemo extends BaseTextEditorToolMenu {
    public static final String KEY_BASE64_FLAGS = "base64Flags";
    public static final String KEY_CHARSET = "charset";

    @NonNull
    @Override
    public String name() {
        return "Base64";
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
        PluginEditTextBuilder builder = pluginUI
                .defaultStyle(pluginUI.getStyle().new Modifier() {
                    @Override
                    protected void handleEditText(PluginUI pluginUI, PluginEditTextBuilder builder) {
                        super.handleEditText(pluginUI, builder);
                        builder.minLines(5).maxLines(10).textSize(12).softWrap(PluginEditText.SOFT_WRAP_KEEP_WORD);
                    }

                    @Override
                    protected void handleButton(PluginUI pluginUI, PluginButtonBuilder builder) {
                        super.handleButton(pluginUI, builder);
                        builder.style(PluginButton.Style.FILLED);
                        builder.text("{base64:" + builder.getId() + "}"); // 使用id设置按钮标题
                    }
                })
                .buildVerticalLayout()
                .paddingTop(pluginUI.dialogPaddingVertical() / 2)
                // 添加输入框1
                .addEditBox("input1").text(selectedText)
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
                .setTitle(name())
                .setView(view)
                .setPositiveButton("{close}", null) // {close} 来自MT内置语言包: MT.apk/assets/strings.mtl
                .setNegativeButton("{base64:exchange}", null)
                .setNeutralButton("{base64:options}", null)
                .show();
        PluginEditText input1 = view.requireViewById("input1");
        PluginEditText input2 = view.requireViewById("input2");
        SharedPreferences preferences = pluginUI.getContext().getPreferences();

        // 编码
        view.requireViewById("encode").setOnClickListener(button -> {
            String text = input1.getText().toString();
            int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
            Charset charset = Charset.forName(preferences.getString(KEY_CHARSET, "UTF-8"));
            input2.setText(Base64.encodeToString(text.getBytes(charset), flags));
        });

        // 解码
        view.requireViewById("decode").setOnClickListener(button -> {
            String text = input1.getText().toString();
            int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
            Charset charset = Charset.forName(preferences.getString(KEY_CHARSET, "UTF-8"));
            try {
                input2.setText(new String(Base64.decode(text.getBytes(charset), flags), charset));
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

        // 选项
        dialog.getNeutralButton().setOnClickListener(button -> {
            PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
            PluginMenu menu = popupMenu.getMenu();

            int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
            String[] charsets = new String[]{"UTF-8", "UTF-16", "GBK", "Big5"};
            String currentCharset = preferences.getString(KEY_CHARSET, "UTF-8");

            // 添加 Base64 Flags 选项组
            menu.add("0", "{base64:flag_no_padding}").setCheckable(true).setChecked((flags & Base64.NO_PADDING) != 0);
            menu.add("1", "{base64:flag_no_wrap}").setCheckable(true).setChecked((flags & Base64.NO_WRAP) != 0);
            menu.add("2", "{base64:flag_url_safe}").setCheckable(true).setChecked((flags & Base64.URL_SAFE) != 0);

            // 添加文本编码选项组
            PluginSubMenu charsetGroup = menu.addSubMenu("charsets", "{base64:charset}");
            for (String charset : charsets) {
                charsetGroup.add(charset, charset, "group").setChecked(currentCharset.equals(charset));
            }
            charsetGroup.setGroupCheckable("group", true, true);

            // 设置菜单点击事件
            popupMenu.setOnMenuItemClickListener(item -> {
                String itemId = item.getItemId();
                if (itemId.equals("charsets")) {
                    return true;
                }
                // 设置文本编码
                if (itemId.length() > 1) {
                    item.setChecked(true);
                    preferences.edit().putString(KEY_CHARSET, item.getItemId()).apply();
                    return true;
                }
                // 设置 Base64 Flags
                item.setChecked(!item.isChecked());
                int selectedFlags = switch (itemId) {
                    case "0" -> Base64.NO_PADDING;
                    case "1" -> Base64.NO_WRAP;
                    case "2" -> Base64.URL_SAFE;
                    default -> throw new RuntimeException();
                };
                int newFlags = preferences.getInt(KEY_BASE64_FLAGS, 0);
                if (item.isChecked()) {
                    newFlags |= selectedFlags;
                } else {
                    newFlags &= ~selectedFlags;
                }
                preferences.edit().putInt(KEY_BASE64_FLAGS, newFlags).apply();
                return true;
            });
            popupMenu.show();
        });

        if (!selectedText.isEmpty()) {
            PluginView replaceButton = view.requireViewById("replace");
            // 第二个输入框内容不为空时才可点击替换原文按钮
            input2.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
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
