package guobao.plugin.translator.deeplx.pref;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Config implements PluginPreference {
  @Override
  public void onBuild(PluginContext context, Builder builder) {
    builder.addHeader("{about}");
    builder.addText("{github}").summary("{github_info}").url(context.getString("{github_url}"));
    builder.addText("{author}");
  }
}