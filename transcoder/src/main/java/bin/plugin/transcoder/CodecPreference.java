package bin.plugin.transcoder;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class CodecPreference implements PluginPreference {

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.title("{plugin_name}");

        builder.addHeader("{function_entry}");

        builder.addSwitch("{enable_in_editor_tool_menu}", "enable_in_editor_tool_menu")
                .summary("{enable_in_editor_tool_menu_summary}")
                .defaultValue(true);

        builder.addSwitch("{enable_in_editor_floating_menu}", "enable_in_editor_floating_menu")
                .summary("{enable_in_editor_floating_menu_summary}")
                .defaultValue(true);

        builder.addSwitch("{enable_in_editor_function}", "enable_in_editor_function")
                .summary("{enable_in_editor_function_summary}")
                .defaultValue(true);

        builder.addText("{function_entry_tip}")
                .summary("{function_entry_tip_summary}");
    }

}
