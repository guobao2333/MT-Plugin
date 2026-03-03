package guobao.plugin.translator.deeplx;

import androidx.annotation.NonNull;

import android.content.SharedPreferences;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.translation.BaseTranslationEngine;

import guobao.plugin.translator.deeplx.pref.*;

public class TranslationEngine extends BaseTranslationEngine {
    private List<String> sourceLanguages = Arrays.asList(DeepLConstant.SOURCE_LANGUAGES);
    private List<String> targetLanguages = Arrays.asList(DeepLConstant.TARGET_LANGUAGES);

    private PluginContext context;

    @Override
    protected void init() {
        context = getContext();
    }

    protected void onBuildConfiguration(ConfigurationBuilder builder) {
        super.onBuildConfiguration(builder);
        builder.setAllowBatchTranslationBySeparator(true);
        builder.setMaxTranslationTextLength(10000);
        //builder.setTargetLanguageMutable(true);
    }

    @Override
    public String name() {
        return "{interface_name_web}";
    }

    @Override
    public List<String> loadSourceLanguages() {
        return sourceLanguages;
    }

    @Override
    public List<String> loadTargetLanguages(String sourceLanguage) {
        return targetLanguages;
    }

    @NonNull
    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {
        try (var translator = new DeepLWebTranslator(context)) {
            DeepLWebTranslator.TranslationResult result =
                    translator.translate(text, sourceLanguage, targetLanguage);
            context.log(result.toString());
            return result.text();
        } catch (DeepLWebTranslator.DeepLException e) {
            context.log(e);
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void onFinish() {}
}
