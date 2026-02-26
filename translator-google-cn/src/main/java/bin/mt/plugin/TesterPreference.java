package bin.mt.plugin;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.util.TranslationLengthLimitTester;

public class TesterPreference implements PluginPreference {
    private final IPLoader ipLoader = new IPLoader();

    @Override
    public void onBuild(PluginContext context, Builder builder) {
        TranslationLengthLimitTester.addToPluginPreference(builder, text -> GoogleCNTranslator.translate(ipLoader, text, "en", "zh"));
    }


}
