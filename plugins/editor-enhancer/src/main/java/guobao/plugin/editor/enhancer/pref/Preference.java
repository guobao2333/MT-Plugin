package guobao.plugin.editor.enhancer.pref;

import android.content.SharedPreferences;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Preference implements PluginPreference {
  public void onBuild(PluginContext context, Builder builder) {

    builder.title("{prefs}").subtitle("{common}");

    builder.addHeader("{about}");
    builder.addText("{github}").summary("{github_info}").url(context.getString("{github_url}"));

  }
}