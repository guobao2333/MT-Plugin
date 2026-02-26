package bin.mt.plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bin.mt.json.JSONArray;
import bin.mt.json.JSONObject;
import bin.mt.plugin.util.UserAgentInterceptor;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Bin
 */
public class BingWebTranslator {
    private static final Map<String, String> LANG_MAP = new HashMap<>();
    private static String domain = "cn.bing.com";
    private static String ig;
    private static String iid;
    private static String token;
    private static String key;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor.INSTANCE)
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    static {
        LANG_MAP.put("auto", "auto-detect");
        LANG_MAP.put("zh-CN", "zh-Hans");
        LANG_MAP.put("zh-TW", "zh-Hant");
        LANG_MAP.put("iw", "he");
        LANG_MAP.put("hmn", "mww");
        LANG_MAP.put("tl", "fil");
    }

    public static void setDomain(String domain) {
        BingWebTranslator.domain = domain;
    }

    public static String translate(String query, String from, String to) throws IOException {
        if (LANG_MAP.containsKey(from)) {
            from = LANG_MAP.get(from);
        }
        if (LANG_MAP.containsKey(to)) {
            to = LANG_MAP.get(to);
        }
        for (int i = 0; i < 2; i++) {
            if (ig == null) {
                init();
            }
            String result = translateImpl(query, from, to);
            if (result.contains("statusCode")) {
                processError(result); // 出现205错误时才会进行下一个循环
            } else {
                return getResult(result);
            }
        }
        throw new IOException("Translation failed");
    }

    private static String translateImpl(String query, String from, String to) throws IOException {
        FormBody body = new FormBody.Builder()
                .add("fromLang", from)
                .add("text", query)
                .add("to", to)
                .add("token", token)
                .add("key", key)
                .build();
        Request request = new Request.Builder()
                .url("https://" + domain + "/ttranslatev3?IG=" + ig + "&IID=" + iid + ".1")
                .post(body)
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new IOException("Empty response");
            return responseBody.string();
        }
    }

    private static String getResult(String result) {
        StringBuilder sb = new StringBuilder();
        JSONArray array = new JSONArray(result);
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (!json.contains("translations")) {
                continue;
            }
            JSONArray translations = json.getJSONArray("translations");
            for (int j = 0; j < translations.size(); j++) {
                sb.append(translations.getJSONObject(j).getString("text"));
            }
        }
        return sb.toString();
    }

    private static void processError(String result) throws IOException {
        JSONObject json = new JSONObject(result);
        int statusCode = json.getInt("statusCode");
        String errorMessage = json.getString("errorMessage");
        if (statusCode != 205) {
            throw new IOException(errorMessage.isEmpty() ? "StatusCode: " + statusCode : errorMessage);
        }
        ig = null;
    }

    private static void init() throws IOException {
        Request request = new Request.Builder()
                .url("https://" + domain + "/translator")
                .build();
        String src;
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new IOException("Empty response");
            src = responseBody.string();
        }
        ig = getMiddleText(src, "IG:\"", "\"");
        iid = getMiddleText(src, "<div id=\"rich_tta\" data-iid=\"", "\"");
        String richTranslateHelper = getMiddleText(src, "params_AbusePreventionHelper = ", ";");
        if (ig == null) {
            throw new IOException("Can not parse parameter: ig");
        }
        if (iid == null) {
            throw new IOException("Can not parse parameter: iid");
        }
        if (richTranslateHelper == null) {
            throw new IOException("Can not parse parameter: richTranslateHelper");
        }
        JSONArray array = new JSONArray(richTranslateHelper);
        token = array.getString(1);
        key = String.valueOf(array.getLong(0));
    }

    private static String getMiddleText(String str, String start, String end) {
        int i = str.indexOf(start);
        if (i == -1) {
            return null;
        }
        i += start.length();
        int j = str.indexOf(end, i);
        if (j == -1) {
            return null;
        }
        return str.substring(i, j);
    }


}
