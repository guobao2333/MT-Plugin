package bin.mt.plugin;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.util.TranslationLengthLimitTester;

public class TesterPreference implements PluginPreference {

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        TranslationLengthLimitTester.addToPluginPreference(builder, text -> YoudaoWebTranslator.translate(text, "en", "zh"));
    }


}
