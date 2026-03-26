package bin.plugin.transcoder.codec;

import androidx.annotation.NonNull;

import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.plugin.transcoder.Codec;

/**
 * Uncode编码解码器
 */
public class UnicodeCodec extends Codec {
    private static final String KEY_UPPERCASE = "uppercase";

    public UnicodeCodec(PluginUI pluginUI) {
        super(pluginUI, "Unicode");
    }

    @Override
    public PluginView.OnClickListener getOnOptionsButtonClickListener(PluginUI pluginUI) {
        return button -> {
            PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
            popupMenu.getMenu().add("uppercase", "{uppercase}").setCheckable(true).setChecked(preferences.getBoolean(KEY_UPPERCASE, true));

            // 设置菜单点击事件
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId().equals("uppercase")) {
                    item.setChecked(!item.isChecked());
                    preferences.edit().putBoolean(KEY_UPPERCASE, item.isChecked()).apply();
                }
                return true;
            });
            popupMenu.show();
        };
    }

    @NonNull
    @Override
    public String encode(@NonNull String text) {
        if (text.isEmpty()) {
            return "";
        }
        String format = preferences.getBoolean(KEY_UPPERCASE, true) ? "%04X" : "%04x";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append("\\u").append(String.format(format, (int) c));
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public String decode(@NonNull String text) {
        if (text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '\\') {
                if (i + 1 >= text.length()) {
                    throw new IllegalArgumentException("Invalid escape at position " + i);
                }
                if (text.charAt(i + 1) == 'u') {
                    if (i + 5 >= text.length()) {
                        throw new IllegalArgumentException("Incomplete unicode sequence at position " + i);
                    }
                    try {
                        String hex = text.substring(i + 2, i + 6);
                        int codePoint = Integer.parseInt(hex, 16);
                        sb.append((char) codePoint);
                        i += 6;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid hex number at position " + (i + 2));
                    }
                } else {
                    sb.append(text.charAt(i));
                    i++;
                }
            } else {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
