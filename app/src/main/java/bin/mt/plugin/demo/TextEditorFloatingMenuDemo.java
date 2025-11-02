package bin.mt.plugin.demo;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import bin.mt.plugin.api.drawable.MaterialIcons;
import bin.mt.plugin.api.editor.BaseTextEditorFloatingMenu;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.ui.PluginUI;

public class TextEditorFloatingMenuDemo extends BaseTextEditorFloatingMenu {
    @NonNull
    @Override
    public String name() {
        return "大小写反转";
    }

    @NonNull
    @Override
    public Drawable icon() {
        // 直接获取内置的Material图标：https://mt2.cn/icons
        return MaterialIcons.get("swap_vert");
        // 也可以加载外部数据
//        return VectorDrawableLoader.fromVectorXml(getContext(), "case.xml");
//        return VectorDrawableLoader.fromSvg(getContext(), "case.svg");
    }

    @Override
    public boolean checkVisible(@NonNull TextEditor editor) {
        // 仅在选中文本时显示菜单
        return editor.hasTextSelected();
    }

    @Override
    public void onMenuClick(@NonNull PluginUI pluginUI, @NonNull TextEditor editor) {
        int from = editor.getSelectionStart();
        int to = editor.getSelectionEnd();
        char[] charArray = editor.subText(from, to).toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (Character.isLowerCase(c)) {
                charArray[i] = Character.toUpperCase(c);
            } else {
                charArray[i] = Character.toLowerCase(c);
            }
        }
        editor.replaceText(from, to, new String(charArray));
    }
}
