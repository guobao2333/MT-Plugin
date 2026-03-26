package bin.plugin.transcoder.codec;

import androidx.annotation.NonNull;

import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.plugin.transcoder.CharsetMenuHelper;
import bin.plugin.transcoder.Codec;

/**
 * Hex 编码解码器。
 */
public class HexCodec extends Codec {
    private static final String KEY_HEX_UPPERCASE = "hexUppercase";
    private static final char[] LOWERCASE_DIGITS = "0123456789abcdef".toCharArray();
    private static final char[] UPPERCASE_DIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * 初始化 Hex 编码解码器。
     */
    public HexCodec(PluginUI pluginUI) {
        super(pluginUI, "Hex");
    }

    /**
     * 提供大小写与字符集选项，只影响编码输出格式与字节转换方式。
     */
    @Override
    public PluginView.OnClickListener getOnOptionsButtonClickListener(PluginUI pluginUI) {
        return button -> {
            PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
            PluginMenu menu = popupMenu.getMenu();
            menu.add("uppercase", "{uppercase}")
                    .setCheckable(true)
                    .setChecked(preferences.getBoolean(KEY_HEX_UPPERCASE, true));
            CharsetMenuHelper.addMenuGroup(menu, preferences);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (CharsetMenuHelper.onMenuItemClick(item, preferences)) {
                    return true;
                }
                if (item.getItemId().equals("uppercase")) {
                    item.setChecked(!item.isChecked());
                    preferences.edit().putBoolean(KEY_HEX_UPPERCASE, item.isChecked()).apply();
                }
                return true;
            });
            popupMenu.show();
        };
    }

    /**
     * 按当前字符集转字节后输出连续 Hex 字符串。
     */
    @NonNull
    @Override
    public String encode(@NonNull String text) throws Exception {
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        byte[] bytes = text.getBytes(charset);
        char[] digits = preferences.getBoolean(KEY_HEX_UPPERCASE, true) ? UPPERCASE_DIGITS : LOWERCASE_DIGITS;
        char[] result = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            result[index++] = digits[(b >>> 4) & 0x0F];
            result[index++] = digits[b & 0x0F];
        }
        return new String(result);
    }

    /**
     * 宽松解析 Hex 文本，允许空白和 0x 前缀后再按当前字符集还原原文。
     */
    @NonNull
    @Override
    public String decode(@NonNull String text) throws Exception {
        String normalized = normalizeHex(text);
        if ((normalized.length() & 1) != 0) {
            throw new IllegalArgumentException("Hex length must be even");
        }
        byte[] bytes = new byte[normalized.length() / 2];
        for (int i = 0; i < normalized.length(); i += 2) {
            int high = Character.digit(normalized.charAt(i), 16);
            int low = Character.digit(normalized.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Invalid hex character at position " + i);
            }
            bytes[i / 2] = (byte) ((high << 4) | low);
        }
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        return new String(bytes, charset);
    }

    /**
     * 清理用户输入中的空白和常见前缀，避免影响后续按字节解析。
     */
    @NonNull
    private String normalizeHex(@NonNull String text) {
        StringBuilder sb = new StringBuilder(text.length());
        int index = 0;
        boolean tokenStart = true;
        while (index < text.length()) {
            char ch = text.charAt(index);
            if (Character.isWhitespace(ch)) {
                tokenStart = true;
                index++;
                continue;
            }
            // 仅在新 token 开头剥离 0x，避免把无效内容误判成合法 Hex。
            if (tokenStart && ch == '0' && index + 1 < text.length()) {
                char next = text.charAt(index + 1);
                if (next == 'x' || next == 'X') {
                    index += 2;
                    tokenStart = false;
                    continue;
                }
            }
            sb.append(ch);
            tokenStart = false;
            index++;
        }
        return sb.toString();
    }
}
