package guobao.plugin.converter;

import android.content.SharedPreferences;

import bin.mt.plugin.api.PluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Setting implements PluginPreference {
  public void onBuild(PluginContext context, Builder builder) {

    builder.addHeader("{case}");
    builder.addHeader("{camel_info}");
    builder.addSwitch("{camel_upper}", "camel_upper")
      .defaultValue(false)
      .summaryOn("{on}")
      .summaryOff("{off}");

    builder.addHeader("{snake_info}");
    builder.addSwitch("{snake_nb}", "snake_number")
      .defaultValue(false)
      .summaryOn("{on_1}")
      .summaryOff("{off_1}");
    builder.addSwitch("{snake_up_c}", "snake_upper_continuous")
      .defaultValue(false)
      .summaryOn("{snake_up_c_on}")
      .summaryOff("{off_1}");

    builder.addHeader("{zshh}");
    builder.addText("{title}").summary("{zshh_info}");
    builder.addText("{title_2}").summary("{zshh_info_2}");


    builder.addHeader("{about}");
    builder.addText("{github}").summary("{github_info}").url("{github_url}");
  }
}