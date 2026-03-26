package bin.plugin.transcoder.codec;

import androidx.annotation.NonNull;

import java.net.URLDecoder;
import java.net.URLEncoder;

import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.plugin.transcoder.CharsetMenuHelper;
import bin.plugin.transcoder.Codec;

/**
 * URL 编码解码器。
 */
public class UrlCodec extends Codec {
    private static final String KEY_SPACE_AS_PLUS = "urlSpaceAsPlus";

    /**
     * 初始化 URL 编码解码器。
     */
    public UrlCodec(PluginUI pluginUI) {
        super(pluginUI, "URL");
    }

    /**
     * 提供空格处理与字符集选项，保证编码和解码语义保持一致。
     */
    @Override
    public PluginView.OnClickListener getOnOptionsButtonClickListener(PluginUI pluginUI) {
        return button -> {
            PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
            PluginMenu menu = popupMenu.getMenu();
            menu.add("space_as_plus", "{url_space_as_plus}")
                    .setCheckable(true)
                    .setChecked(preferences.getBoolean(KEY_SPACE_AS_PLUS, true));
            CharsetMenuHelper.addMenuGroup(menu, preferences);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (CharsetMenuHelper.onMenuItemClick(item, preferences)) {
                    return true;
                }
                if (item.getItemId().equals("space_as_plus")) {
                    item.setChecked(!item.isChecked());
                    preferences.edit().putBoolean(KEY_SPACE_AS_PLUS, item.isChecked()).apply();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        };
    }

    /**
     * 根据空格处理选项执行 URL 编码，兼容表单模式与 %20 模式。
     */
    @NonNull
    @Override
    public String encode(@NonNull String text) throws Exception {
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        String encoded = URLEncoder.encode(text, charset);
        if (preferences.getBoolean(KEY_SPACE_AS_PLUS, true)) {
            return encoded;
        }
        // 关闭 + 模式时仅替换空格产生的 +，原始加号此前已编码为 %2B。
        return encoded.replace("+", "%20");
    }

    /**
     * 根据空格处理选项执行 URL 解码，避免在 %20 模式下误把 + 还原为空格。
     */
    @NonNull
    @Override
    public String decode(@NonNull String text) throws Exception {
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        if (!preferences.getBoolean(KEY_SPACE_AS_PLUS, true)) {
            // 先转义字面 +，再复用 URLDecoder 处理 %xx 序列。
            text = text.replace("+", "%2B");
        }
        return URLDecoder.decode(text, charset);
    }
}
