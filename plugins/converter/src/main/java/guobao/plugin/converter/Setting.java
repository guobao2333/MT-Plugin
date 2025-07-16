package guobao.plugin.converter;

import android.content.SharedPreferences;

import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Setting implements PluginPreference {
  @Override
  public void onBuild(MTPluginContext context, Builder builder) {
    SharedPreferences settings = context.getPreferences();
    builder.setLocalString(context.getLocalString());

    builder.addHeader("{zshh}");
    builder.addText("{title}").summary("{zshh_info}");
    builder.addText("{title_2}").summary("{zshh_info_2}");
    
    /*builder.addSwitch("{}", "key_switch")
      .defaultValue(true)
      .summaryOn("开")
      .summaryOff("关");
    if (settings.getBoolean("key_switch", false)) {
    
    }*/
      
    builder.addHeader("{about}");
    builder.addText("{github}").summary("{github_info}").url("{github_url}");
  }
}