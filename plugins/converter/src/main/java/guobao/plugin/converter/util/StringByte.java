package guobao.plugin.converter.util;

import java.io.*;

public class StringByte {
    /**
     * 将String转换为byte[]
     */
    public static byte[] toByte(String str, String encoding) throws UnsupportedEncodingException {
        return str.getBytes(encoding);
    }

    /**
     * 将byte[]转换为String
     */
    public static String toString(byte[] bytes, String encoding) throws UnsupportedEncodingException {
        return new String(bytes, encoding);
    }
}