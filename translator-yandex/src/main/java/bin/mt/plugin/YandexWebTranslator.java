package bin.mt.plugin;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import bin.mt.json.JSONArray;
import bin.mt.json.JSONObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class YandexWebTranslator {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                String ua = System.getProperty("http.agent");
                if (ua == null) {
                    ua = "Mozilla/5.0 (Linux; Android 8.0;)";
                }
                Request request = chain.request().newBuilder()
                        .header("User-Agent", ua)
                        .build();
                return chain.proceed(request);
            })
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    public static String translate(String query, String from, String to) throws IOException {
        String lang;
        if (from.equals("auto"))
            lang = to;
        else
            lang = from + '-' + to;
        FormBody body = new FormBody.Builder()
                .add("srv", "android")
                .add("ucid", UUID.randomUUID().toString().replace("-", ""))
                .add("lang", lang)
                .add("text", query)
                .build();
        Request request = new Request.Builder()
                .url("https://translate.yandex.net/api/v1/tr.json/translate")
                .post(body)
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new IOException("Empty response");
            JSONObject result = new JSONObject(responseBody.string());
            return getResult(result);
        }
    }

    private static String getResult(JSONObject json) throws IOException {
        int code = json.getInt("code");
        if (code != 200) {
            throw new IOException("Error " + code + ": " + json.getString("message"));
        }
        JSONArray array = json.getJSONArray("text");
        StringBuilder sb = new StringBuilder();
        int size = array.size();
        for (int i = 0; i < size; i++) {
            sb.append(array.getString(i));
        }
        return sb.toString();
    }

}
