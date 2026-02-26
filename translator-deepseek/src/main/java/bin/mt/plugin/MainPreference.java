package bin.mt.plugin;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.ui.dialog.LoadingDialog;

public class MainPreference implements PluginPreference {

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        builder.addHeader("{plugin_name}");

        builder.addInput("{ds_api_key}", DeepSeekConstant.DEEPSEEK_API_KEY_PREFERENCE_KEY)
                .defaultValue(DeepSeekConstant.DEEPSEEK_API_KEY_DEFAULT)
                .summary("{ds_api_key_summary}");

        builder.addText("{ds_test_title}").summary("{ds_test_summary}").onClick((pluginUI, preferenceItem) -> {
            String apiKey = context.getPreferences().getString(
                    DeepSeekConstant.DEEPSEEK_API_KEY_PREFERENCE_KEY,
                    DeepSeekConstant.DEEPSEEK_API_KEY_DEFAULT
            );
            if (apiKey.trim().isEmpty()) {
                pluginUI.showToastL("{ds_api_key_empty}");
                return;
            }
            LoadingDialog loadingDialog = new LoadingDialog(pluginUI).setMessage("{ds_test_loading}").show();
            new Thread(() -> {
                try {
                    String translated = DeepSeekTranslationEngine.requestTranslation(apiKey.trim(), "apple", "English", "Chinese");
                    if (translated.trim().isEmpty()) {
                        pluginUI.showToastL("{ds_test_empty_result}");
                    } else {
                        pluginUI.showToastL(context.getString("{ds_test_success}") + "apple → " + translated);
                    }
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message == null || message.trim().isEmpty()) {
                        message = e.toString();
                    }
                    pluginUI.showToastL(context.getString("{ds_test_failed}") + message);
                } finally {
                    loadingDialog.dismiss();
                }
            }).start();
        });

        builder.addText("{reg_url}")
                .summary("{reg_url_summary}")
                .url("https://platform.deepseek.com/api_keys");
    }
}
