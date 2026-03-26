package bin.plugin.transcoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bin.mt.json.JSONObject;
import bin.mt.plugin.api.editor.TextEditor;
import bin.mt.plugin.api.editor.TextEditorFunction;
import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;

/**
 * 文本编辑器快捷功能接口
 */
public class CodecEditorFunction extends CodecBaseMenu implements TextEditorFunction {

    @Override
    public boolean isEnabled() {
        return getContext().getPreferences().getBoolean("enable_in_editor_function", true);
    }

    @Override
    public boolean supportEditTextView() {
        return true;
    }

    @Override
    public boolean supportRepeat() {
        return false;
    }

    @Nullable
    @Override
    public PluginView buildOptionsView(@NonNull PluginUI pluginUI, @Nullable JSONObject data) {
        return null;
    }

    @Nullable
    @Override
    public JSONObject getOptionsData(@NonNull PluginUI pluginUI, @NonNull PluginView pluginView) {
        return null;
    }

    @Override
    public void doFunction(@NonNull PluginUI pluginUI, @NonNull TextEditor editor, @Nullable JSONObject data) {
        onMenuClick(pluginUI, editor);
    }
}
