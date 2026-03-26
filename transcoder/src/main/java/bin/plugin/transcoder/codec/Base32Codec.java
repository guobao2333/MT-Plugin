package bin.plugin.transcoder.codec;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;

import bin.mt.plugin.api.ui.PluginUI;
import bin.mt.plugin.api.ui.PluginView;
import bin.mt.plugin.api.ui.menu.PluginMenu;
import bin.mt.plugin.api.ui.menu.PluginPopupMenu;
import bin.plugin.transcoder.CharsetMenuHelper;
import bin.plugin.transcoder.Codec;

/**
 * Base32 编码解码器。
 */
public class Base32Codec extends Codec {
    private static final String KEY_BASE32_UPPERCASE = "base32Uppercase";
    private static final String KEY_BASE32_NO_PADDING = "base32NoPadding";
    private static final char[] UPPERCASE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final char[] LOWERCASE_ALPHABET = "abcdefghijklmnopqrstuvwxyz234567".toCharArray();

    /**
     * 初始化 Base32 编码解码器。
     */
    public Base32Codec(PluginUI pluginUI) {
        super(pluginUI, "Base32");
    }

    /**
     * 提供大小写、填充和字符集选项，只影响编码输出与字节转换方式。
     */
    @Override
    public PluginView.OnClickListener getOnOptionsButtonClickListener(PluginUI pluginUI) {
        return button -> {
            PluginPopupMenu popupMenu = pluginUI.createPopupMenu(button);
            PluginMenu menu = popupMenu.getMenu();
            menu.add("uppercase", "{uppercase}")
                    .setCheckable(true)
                    .setChecked(preferences.getBoolean(KEY_BASE32_UPPERCASE, true));
            menu.add("no_padding", "{flag_no_padding}")
                    .setCheckable(true)
                    .setChecked(preferences.getBoolean(KEY_BASE32_NO_PADDING, false));
            CharsetMenuHelper.addMenuGroup(menu, preferences);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (CharsetMenuHelper.onMenuItemClick(item, preferences)) {
                    return true;
                }
                if (item.getItemId().equals("uppercase")) {
                    item.setChecked(!item.isChecked());
                    preferences.edit().putBoolean(KEY_BASE32_UPPERCASE, item.isChecked()).apply();
                    return true;
                }
                if (item.getItemId().equals("no_padding")) {
                    item.setChecked(!item.isChecked());
                    preferences.edit().putBoolean(KEY_BASE32_NO_PADDING, item.isChecked()).apply();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        };
    }

    /**
     * 按当前字符集转成字节后执行 Base32 编码，可根据选项控制大小写与填充。
     */
    @NonNull
    @Override
    public String encode(@NonNull String text) throws Exception {
        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        byte[] bytes = text.getBytes(charset);
        if (bytes.length == 0) {
            return "";
        }
        char[] alphabet = preferences.getBoolean(KEY_BASE32_UPPERCASE, true) ? UPPERCASE_ALPHABET : LOWERCASE_ALPHABET;
        boolean noPadding = preferences.getBoolean(KEY_BASE32_NO_PADDING, false);
        StringBuilder sb = new StringBuilder(((bytes.length + 4) / 5) * 8);
        int buffer = 0;
        int bitCount = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitCount += 8;
            while (bitCount >= 5) {
                sb.append(alphabet[(buffer >> (bitCount - 5)) & 0x1F]);
                bitCount -= 5;
            }
            // 仅保留尚未输出的低位，避免长文本累计移位后污染结果。
            buffer = bitCount == 0 ? 0 : (buffer & ((1 << bitCount) - 1));
        }
        if (bitCount > 0) {
            sb.append(alphabet[(buffer << (5 - bitCount)) & 0x1F]);
        }
        if (!noPadding) {
            while ((sb.length() & 7) != 0) {
                sb.append('=');
            }
        }
        return sb.toString();
    }

    /**
     * 兼容大小写、空白和可选填充的 Base32 文本，再按当前字符集还原内容。
     */
    @NonNull
    @Override
    public String decode(@NonNull String text) throws Exception {
        String compact = removeWhitespace(text);
        if (compact.isEmpty()) {
            return "";
        }

        int paddingStart = compact.indexOf('=');
        String dataPart = compact;
        if (paddingStart >= 0) {
            for (int i = paddingStart; i < compact.length(); i++) {
                if (compact.charAt(i) != '=') {
                    throw new IllegalArgumentException("Invalid Base32 padding at position " + i);
                }
            }
            if ((compact.length() & 7) != 0) {
                throw new IllegalArgumentException("Invalid Base32 padding length");
            }
            dataPart = compact.substring(0, paddingStart);
            int paddingCount = compact.length() - paddingStart;
            if (paddingCount != expectedPaddingCount(dataPart.length())) {
                throw new IllegalArgumentException("Invalid Base32 padding size");
            }
        }

        int remainder = dataPart.length() & 7;
        if (remainder == 1 || remainder == 3 || remainder == 6) {
            throw new IllegalArgumentException("Invalid Base32 length");
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(dataPart.length() * 5 / 8);
        int buffer = 0;
        int bitCount = 0;
        for (int i = 0; i < dataPart.length(); i++) {
            int value = decodeValue(dataPart.charAt(i));
            if (value < 0) {
                throw new IllegalArgumentException("Invalid Base32 character at position " + i);
            }
            buffer = (buffer << 5) | value;
            bitCount += 5;
            while (bitCount >= 8) {
                output.write((buffer >> (bitCount - 8)) & 0xFF);
                bitCount -= 8;
            }
            // 仅保留未输出的有效位，便于校验尾部补零是否正确。
            buffer = bitCount == 0 ? 0 : (buffer & ((1 << bitCount) - 1));
        }
        if (buffer != 0) {
            throw new IllegalArgumentException("Invalid Base32 tail bits");
        }

        String charset = CharsetMenuHelper.getCurrentCharset(preferences);
        return output.toString(charset);
    }

    /**
     * 移除用户输入中的空白，允许复制带换行的 Base32 文本后直接解码。
     */
    @NonNull
    private String removeWhitespace(@NonNull String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isWhitespace(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 根据有效字符数量推导标准 Base32 的填充长度，兼容无填充输入时的长度校验。
     */
    private int expectedPaddingCount(int dataLength) {
        return switch (dataLength & 7) {
            case 0 -> 0;
            case 2 -> 6;
            case 4 -> 4;
            case 5 -> 3;
            case 7 -> 1;
            default -> -1;
        };
    }

    /**
     * 解析单个 Base32 字符，解码时统一兼容大小写。
     */
    private int decodeValue(char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return ch - 'A';
        }
        if (ch >= 'a' && ch <= 'z') {
            return ch - 'a';
        }
        if (ch >= '2' && ch <= '7') {
            return ch - '2' + 26;
        }
        return -1;
    }
}
