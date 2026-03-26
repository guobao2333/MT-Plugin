package bin.plugin.transcoder;

import bin.mt.plugin.api.editor.TextEditorToolMenu;

/**
 * 文本编辑器工具菜单接口
 */
public class CodecEditorToolMenu extends CodecBaseMenu implements TextEditorToolMenu {

    @Override
    public boolean isEnabled() {
        return getContext().getPreferences().getBoolean("enable_in_editor_tool_menu", true);
    }

}
