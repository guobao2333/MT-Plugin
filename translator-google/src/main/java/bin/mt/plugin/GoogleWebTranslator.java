package bin.mt.plugin;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import bin.mt.json.JSONArray;
import bin.mt.plugin.util.UserAgentInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Bin
 */
public class GoogleWebTranslator {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor.INSTANCE)
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    public static String translate(String query, String from, String to) throws IOException {
        //noinspection CharsetObjectCanBeUsed
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t" +
                "&sl=" + from +
                "&tl=" + to +
                "&q=" + URLEncoder.encode(query, "UTF-8");
        Request request = new Request.Builder().url(url).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new IOException("Empty response");
            return getResult(responseBody.string());
        }
    }

    private static String getResult(String string) throws IOException {
        if (!string.startsWith("[[[")) {
            throw new IOException("Parse result failed: " + string);
        }
        JSONArray array = new JSONArray(string).getJSONArray(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            sb.append(array.getJSONArray(i).getString(0));
        }
        return sb.toString();
    }

}
