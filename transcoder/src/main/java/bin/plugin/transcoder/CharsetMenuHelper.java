package bin.plugin.transcoder;

import android.content.SharedPreferences;

import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginMenuItem;
import bin.mt.plugin.api.ui.menu.PluginSubMenu;

public class CharsetMenuHelper {
    private static final String KEY_CHARSET = "charset";
    private static final String[] charsets = new String[]{"UTF-8", "UTF-16", "GBK", "Big5"};

    public static void addMenuGroup(PluginMenu menu, SharedPreferences preferences) {
        String currentCharset = getCurrentCharset(preferences);
        // 添加文本编码选项组
        PluginSubMenu charsetGroup = menu.addSubMenu("charsets", "{charset}");
        for (String charset : charsets) {
            charsetGroup.add("charset_" + charset, charset, "charsetGroup").setChecked(currentCharset.equals(charset));
        }
        charsetGroup.setGroupCheckable("charsetGroup", true, true);
    }

    public static String getCurrentCharset(SharedPreferences preferences) {
        String charset = preferences.getString(KEY_CHARSET, charsets[0]);
        for (String s : charsets) {
            if (s.equals(charset)) {
                return s;
            }
        }
        return charsets[0];
    }

    public static boolean onMenuItemClick(PluginMenuItem item, SharedPreferences preferences) {
        String itemId = item.getItemId();
        if (itemId.equals("charsets")) {
            return true;
        }
        if (itemId.startsWith("charset_")) {
            item.setChecked(true);
            preferences.edit().putString(KEY_CHARSET, itemId.substring(8)).apply();
            return true;
        }
        return false;
    }

}
