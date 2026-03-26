package bin.plugin.transcoder.codec;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.plugin.transcoder.CharsetMenuHelper;
import bin.plugin.transcoder.Codec;

/**
 * Base64编码解码器
 */
public class Base64Codec extends Codec {
    public static final String KEY_BASE64_FLAGS = "base64Flags";

    public Base64Codec(PluginUI pluginUI) {
        super(pluginUI, "Base64");
    }

    @Nullable
    @Override
    public PluginView.OnClickListener getOnOptionsButtonClickListener(PluginUI pluginUI) {
        return button -> {
            PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
            PluginMenu menu = popupMenu.getMenu();

            int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);

            // 添加 Base64 Flags 选项组
            menu.add("0", "{flag_no_padding}").setCheckable(true).setChecked((flags & Base64.NO_PADDING) != 0);
            menu.add("1", "{flag_no_wrap}").setCheckable(true).setChecked((flags & Base64.NO_WRAP) != 0);
            menu.add("2", "{flag_url_safe}").setCheckable(true).setChecked((flags & Base64.URL_SAFE) != 0);

            CharsetMenuHelper.addMenuGroup(menu, preferences);

            // 设置菜单点击事件
            popupMenu.setOnMenuItemClickListener(item -> {
                if (CharsetMenuHelper.onMenuItemClick(item, preferences)) {
                    return true;
                }
                // 设置 Base64 Flags
                item.setChecked(!item.isChecked());
                int selectedFlags = switch (item.getItemId()) {
                    case "0" -> Base64.NO_PADDING;
                    case "1" -> Base64.NO_WRAP;
                    case "2" -> Base64.URL_SAFE;
                    default -> throw new RuntimeException();
                };
                int newFlags = preferences.getInt(KEY_BASE64_FLAGS, 0);
                if (item.isChecked()) {
                    newFlags |= selectedFlags;
                } else {
                    newFlags &= ~selectedFlags;
                }
                preferences.edit().putInt(KEY_BASE64_FLAGS, newFlags).apply();
                return true;
            });
            popupMenu.show();
        };
    }

    @NonNull
    @Override
    public String encode(@NonNull String text) throws Exception {
        int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        return Base64.encodeToString(text.getBytes(charset), flags);
    }

    @NonNull
    @Override
    public String decode(@NonNull String text) throws Exception {
        int flags = preferences.getInt(KEY_BASE64_FLAGS, 0);
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        return new String(Base64.decode(text, flags), charset);
    }

}
