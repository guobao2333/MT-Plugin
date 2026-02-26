package bin.mt.plugin;

import android.util.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import bin.mt.json.JSONArray;
import bin.mt.json.JSONObject;
import bin.mt.plugin.util.UserAgentInterceptor;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 有道翻译Web版
 *
 * @author Bin
 */
@SuppressWarnings("CharsetObjectCanBeUsed")
public class YoudaoWebTranslator {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor.INSTANCE)
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    private static String cookie;
    private static String secretKey;
    private static byte[] aesKey;
    private static byte[] aesIv;

    public static String translate(String query, String from, String to) throws IOException {
        if (from.equals("zh")) {
            from = "zh-CHS";
        }
        if (to.equals("zh")) {
            to = "zh-CHS";
        }
        if (cookie == null) {
            Request cookieRequest = new Request.Builder()
                    .url("https://fanyi.youdao.com/")
                    .build();
            try (Response cookieResponse = HTTP_CLIENT.newCall(cookieRequest).execute()) {
                String str = cookieResponse.header("Set-Cookie");
                if (str == null) {
                    throw new IOException("Can not get cookie");
                }
                int i = str.indexOf(';');
                cookie = i == -1 ? str : str.substring(0, i);
            }

            long time = System.currentTimeMillis();
            String url = "https://dict.youdao.com/webtranslate/key?keyid=webfanyi-key-getter-2025&sign=" + sign("yU5nT5dK3eZ1pI4j", time)
                    + "&client=fanyideskweb&product=webfanyi&appVersion=1.0.0&vendor=web&pointParam=client,mysticTime,product&mysticTime=" + time
                    + "&keyfrom=fanyi.web&mid=1&screen=1&model=1&network=wifi&abtest=0&yduuid=abcdefg";

            Request keyRequest = new Request.Builder()
                    .url(url)
                    .header("Cookie", cookie)
                    .header("Referer", "https://fanyi.youdao.com/")
                    .build();
            try (Response keyResponse = HTTP_CLIENT.newCall(keyRequest).execute()) {
                ResponseBody keyBody = keyResponse.body();
                if (keyBody == null) throw new IOException("Empty response");
                JSONObject data = new JSONObject(keyBody.string());

                if (data.getInt("code") != 0) {
                    throw new IOException(data.getString("msg"));
                }
                data = data.getJSONObject("data");
                secretKey = data.getString("secretKey");
                aesKey = md5(data.getString("aesKey"));
                aesIv = md5(data.getString("aesIv"));
            }
        }
        try {
            long time = System.currentTimeMillis();
            FormBody formBody = new FormBody.Builder()
                    .add("i", query)
                    .add("from", from)
                    .add("to", to)
                    .add("useTerm", "false")
                    .add("domain", "0")
                    .add("dictResult", "true")
                    .add("keyid", "webfanyi")
                    .add("sign", sign(secretKey, time))
                    .add("client", "fanyideskweb")
                    .add("product", "webfanyi")
                    .add("appVersion", "1.0.0")
                    .add("vendor", "web")
                    .add("pointParam", "client,mysticTime,product")
                    .add("mysticTime", String.valueOf(time))
                    .add("keyfrom", "fanyi.web")
                    .add("mid", "1")
                    .add("screen", "1")
                    .add("model", "1")
                    .add("network", "wifi")
                    .add("abtest", "0")
                    .add("yduuid", "abcdefg")
                    .build();
            Request translateRequest = new Request.Builder()
                    .url("https://dict.youdao.com/webtranslate")
                    .header("Cookie", cookie)
                    .header("Referer", "https://fanyi.youdao.com/")
                    .post(formBody)
                    .build();
            String base64;
            try (Response translateResponse = HTTP_CLIENT.newCall(translateRequest).execute()) {
                ResponseBody translateBody = translateResponse.body();
                if (translateBody == null) throw new IOException("Empty response");
                base64 = translateBody.string()
                        .replace('_', '/')
                        .replace('-', '+');
            }
            byte[] encryptedBytes = Base64.decode(base64, Base64.DEFAULT);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesIv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            JSONObject result = new JSONObject(new String(decrypted, "UTF-8"));
            switch (result.getInt("code")) {
                case 0:
                    JSONArray array = result.getJSONArray("translateResult");
                    StringBuilder sb = new StringBuilder();
                    int size = array.size();
                    for (int i = 0; i < size; i++) {
                        sb.append(array.getJSONArray(i).getJSONObject(0).getString("tgt"));
                    }
                    return sb.toString();
                case 50:
                    cookie = null;
                    break;
            }
            throw new IOException(result.toString());
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    private static String sign(String key, long time) {
        return toHex(md5("client=fanyideskweb&mysticTime=" + time + "&product=webfanyi&key=" + key));
    }

    private static byte[] md5(String content) {
        try {
            return MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] digest) {
        String hex = "0123456789abcdef";
        char[] str = new char[digest.length * 2];
        int k = 0;
        for (byte b : digest) {
            str[k++] = hex.charAt(b >>> 4 & 0xf);
            str[k++] = hex.charAt(b & 0xf);
        }
        return new String(str);
    }

}
