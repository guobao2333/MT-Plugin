package guobao.plugin.converter.util;

import android.content.SharedPreferences;

import java.io.*;
import java.util.Arrays;

import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class ZshHist {
    private static final byte NULL = (byte) 0x00;
    private static final byte META = (byte) 0x83;
    private static final byte MARKER = (byte) 0xA2;
    private static final boolean[] isMeta = new boolean[256];
    private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区

    private LocalString string;
    private MTPluginContext context;

    static {
        // 初始化特殊字符标记数组
        isMeta[NULL & 0xFF] = true;
        for (int b = META & 0xFF; b <= (MARKER & 0xFF); b++) {
            isMeta[b] = true;
        }
    }

    public ZshHist(MTPluginContext context) {
        this.context = context;
        this.string = context.getAssetLocalString("String");
    }

    public static void main(String[] args) {}

    // 转义特殊字符
    public static byte[] metafy(byte[] input) {
        if (input == null) return new byte[0];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte b : input) {
            int unsignedByte = b & 0xFF; // 转为无符号整数
            if (isMeta[unsignedByte]) {
                out.write(META);
                out.write(b ^ 0x20); // 异或
            } else {
                out.write(b);
            }
        }
        return out.toByteArray();
    }

    // 反转义字符
    public static byte[] unmetafy(byte[] input) {
        if (input == null) return new byte[0];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < input.length; i++) {
            byte current = input[i];
            if (current == META && i + 1 < input.length) {
                // 处理转义序列
                out.write(input[i + 1] ^ 0x20);
                i++; // 跳过下一个字节
            } else {
                out.write(current);
            }
        }
        return out.toByteArray();
    }

    // 辅助方法
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    // 二进制流
    public void process(String i, String o) throws IOException {
        process(i, o, false);
    }

    public void process(String inputPath, String outputPath, boolean doMetafy) throws IOException {
        File inFile = new File(inputPath);
        // if (!inFile.exists() || outputPath.isEmpty())
        if (!inFile.exists()) throw new FileNotFoundException(string.get("error_nff"));
        File outFile = new File(outputPath);
        InputStream in = new FileInputStream(inputPath);
        OutputStream out = new FileOutputStream(outFile);

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                byte[] processed = doMetafy ? metafy(chunk) : unmetafy(chunk);
                out.write(processed);
            }
        } finally {
            out.flush();
            in.close();
            out.close();
        }
    }
}