package guobao.plugin.translator.ai;

import android.content.SharedPreferences;

import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Setting implements PluginPreference {
  @Override
  public void onBuild(MTPluginContext context, Builder builder) {
    SharedPreferences Settings = context.getPreferences();
    builder.setLocalString(context.getLocalString());

    builder.addHeader("{chatgpt_long}");
    builder.addList("{chatgpt}", "chatgpt_model")
      .defaultValue("chatgpt_model_1")
      .addItem("{chatgpt_1}", "chatgpt_model_1").summary("{chatgpt_1}")
      .addItem("{chatgpt_2}", "chatgpt_model_2").summary("{chatgpt_2}");
    builder.addInput("{key}", "chatgpt_api_key").summary("{key_long}");


    builder.addHeader("{deepseek_long}");
    builder.addList("{deepseek}", "deepseek_model")
      .defaultValue("deepseek_model_1")
      .addItem("{deepseek_1}", "deepseek-chat").summary("{deepseek_1}")
      .addItem("{deepseek_2}", "deepseek-reasoner").summary("{deepseek_2}");
    builder.addInput("{key}", "deepseek_api_key").summary("{key_long}");


    builder.addHeader("{gemini_long}");
    builder.addList("{gemini}", "gemini_model")
      .defaultValue("gemini-2.5-flash-lite")
      .addItem("{gemini_1}", "gemini-2.5-flash-lite").summary("{gemini_1}")
      .addItem("{gemini_2}", "gemini-2.5-flash").summary("{gemini_2}")
      .addItem("{gemini_3}", "gemini-2.5-pro").summary("{gemini_3}");
    builder.addInput("{key}", "gemini_api_key").summary("{key_long}");


    builder.addHeader("{qwen_long}");
    builder.addList("{qwen}", "qwen_model")
      .defaultValue("qwen_model_1")
      .addItem("{qwen_1}", "qwen_model_1").summary("{qwen_1}")
      .addItem("{qwen_2}", "qwen_model_2").summary("{qwen_2}");
    builder.addInput("{key}", "qwen_api_key").summary("{key_long}");
    
    builder.addHeader("{about}");
    builder.addText("{github}")
      .summary("{github_text}")
      .url("{github_url}");
  }
}