package guobao.plugin.converter.preference;

import android.content.SharedPreferences;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Preference implements PluginPreference {
  public void onBuild(PluginContext context, Builder builder) {

    builder.addHeader("{case}");
    builder.addHeader("{case_info}");
    builder.addSwitch("{upper_continuous}", "upper_continuous")
      .defaultValue(true)
      .summaryOn("{on_1_default}")
      .summaryOff("{off_1}");
    builder.addSwitch("{split_number}", "split_number")
      .defaultValue(false)
      .summaryOn("{on_1}")
      .summaryOff("{off_1_default}");

    builder.addHeader("{camel_info}");
    builder.addSwitch("{camel_upper}", "camel_upper")
      .defaultValue(false)
      .summaryOn("{on}")
      .summaryOff("{off_default}");

    builder.addHeader("{zshh}");
    builder.addText("{title}").summary("{zshh_info}");
    builder.addText("{title_2}").summary("{zshh_info_2}");


    builder.addHeader("{about}");
    builder.addText("{github}").summary("{github_info}").url(context.getString("{github_url}"));
  }
}