package bin.plugin.transcoder;

import bin.mt.plugin.api.editor.TextEditorFloatingMenu;

/**
 * 文本编辑器浮动菜单接口
 */
public class CodecEditorFloatingMenu extends CodecBaseMenu implements TextEditorFloatingMenu {

    @Override
    public boolean isEnabled() {
        return getContext().getPreferences().getBoolean("enable_in_editor_floating_menu", true);
    }
}
