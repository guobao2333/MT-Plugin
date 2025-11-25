package guobao.plugin.editor.enhancer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.ForegroundColorSpan;
import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.drawable.VectorDrawableLoader;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginUI;

import guobao.plugin.editor.enhancer.format.IndentationAligner;

public class TextEditorFloatingMenu extends BaseTextEditorFloatingMenu {

    @NonNull
    @Override
    public String name() {
        return "{reindent}";
    }

    @NonNull
    @Override
    public Drawable icon() {
        // 直接获取内置的Material图标：https://mt2.cn/icons
        return MaterialIcons.get("format_indent_increase");
    }

    @Override
    public boolean isEnabled() {
        // 是否启用
        return true;
    }

    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        // 仅在选中文本时显示菜单
        return editor.hasTextSelected();
    }

    // 浮窗设置界面中的按钮
    @Override
    public void onPluginButtonClick(@NonNull PluginUI pluginUI) {
        PluginContext context = getContext();
        String pluginId = context.getPluginId();
        int idLen = pluginId.length();
        SpannableString message = new SpannableString(pluginId + "\n\n" + getClass().getName());
        message.setSpan(new RelativeSizeSpan(0.1f), idLen + 1, idLen + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE); // 增加行距
        message.setSpan(new ForegroundColorSpan(pluginUI.colorTextSecondary()), idLen + 2, message.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        message.setSpan(new RelativeSizeSpan(0.8f), idLen + 2, message.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        pluginUI.buildDialog()
                .setTitle(context.getPluginName())
                .setMessage(message)
                .setPositiveButton("{close}", null)
                .show();
    }

    // 主实现
    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        int from = editor.getSelectionStart();
        int to = editor.getSelectionEnd();
        char[] charArray = editor.subText(from, to).toCharArray();
        String charStr = new String(charArray);
        getContext().log(String.join("\n", charStr));
        List<String> str = IndentationAligner.alignFromString(charStr, IndentationAligner.Mode.REMOVE_COMMA);
        //getContext().log(str);
        CharSequence result = String.join("\n", str);
        getContext().log("result: \n"+result);
        editor.replaceText(from, to, result);
    }
}
