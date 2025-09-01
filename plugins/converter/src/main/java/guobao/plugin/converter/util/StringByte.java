package guobao.plugin.converter.util;

import java.io.*;

public class StringByte {
    // String → byte[]
    public static byte[] toByte(String str, String encoding) throws UnsupportedEncodingException {
        return str.getBytes(encoding);
    }

    // byte[] → String
    public static String toString(byte[] bytes, String encoding) throws UnsupportedEncodingException {
        return new String(bytes, encoding);
    }
}