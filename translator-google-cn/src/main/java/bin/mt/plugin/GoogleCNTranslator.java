package bin.mt.plugin;

import android.os.SystemClock;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import bin.mt.json.JSONArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Bin
 */
public class GoogleCNTranslator {
    private static String okIP;
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    public static synchronized String getIP(IPLoader ipLoader, boolean tryAgain) throws IOException {
        String ip = okIP;
        if (ip != null) {
            return ip;
        }
        List<String> ips = ipLoader.load();
        final AtomicReference<String> newIP = new AtomicReference<>();
        final CountDownLatch countDownLatch = new CountDownLatch(ips.size());
        for (final String s : ips) {
            new Thread(() -> {
                try {
                    String url = "http://" + s + "/translate_a/single?client=gtx&dt=t&sl=en&tl=zh&q=apple";
                    Request request = new Request.Builder()
                            .get()
                            .url(url)
                            .header("Host", "translate.googleapis.com")
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                            .build();
                    try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            if (body != null) {
                                String str = body.string();
                                if (str.contains("苹果")) {
                                    System.out.println("Success " + s);
                                    newIP.compareAndSet(null, s);
                                    return;
                                }
                            }
                        }
                    }
                    System.out.println("Fail " + s);
                } catch (Exception e) {
                    System.out.println("Error " + s);
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        while (true) {
            boolean finished;
            try {
                finished = countDownLatch.await(100, TimeUnit.MICROSECONDS);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
            ip = newIP.get();
            if (ip != null) {
                // 测试时已经调用了一次翻译，这里稍微等一下，避免频率过快
                SystemClock.sleep(500);
                return okIP = ip;
            }
            if (finished) {
                ipLoader.clearCache();
                if (tryAgain) {
                    return getIP(ipLoader, false);
                }
                throw new IOException("无可用 IP");
            }
        }
    }

    public static String translate(IPLoader ipLoader, String query, String from, String to) throws IOException {
        for (int i = 0; true; i++) {
            //noinspection CharsetObjectCanBeUsed
            String url = "http://" + getIP(ipLoader, true) + "/translate_a/single?client=gtx&dt=t" +
                    "&sl=" + from +
                    "&tl=" + to +
                    "&q=" + URLEncoder.encode(query, "UTF-8");
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .header("Host", "translate.googleapis.com")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                    .build();
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP response code: " + response.code());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("Body is null");
                }
                return getResult(body.string());
            } catch (IOException e) {
                okIP = null;
                if (i == 0 && e.getMessage() != null && e.getMessage().toLowerCase().contains("connection reset")) {
                    // 出现 Connection reset 错误时换个 IP 重试一次
                    continue;
                }
                throw e;
            }
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
