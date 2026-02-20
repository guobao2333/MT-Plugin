package bin.mt.plugin;

import static bin.mt.plugin.GoogleCNTranslator.HTTP_CLIENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class IPLoader {
    protected List<String> cache;
    protected long lastLoadIPOnlineTime;

    public List<String> load() throws IOException {
        List<String> fromCache = loadFromCache();
        if (fromCache != null && !fromCache.isEmpty()) {
            return fromCache;
        }
        // 一小时内没必要重新获取IP，返回结果是一样的
        if (Math.abs(System.currentTimeMillis() - lastLoadIPOnlineTime) < 3600_000L) {
            throw new IOException("无可用 IP");
        }
        Request request = new Request.Builder()
                .get()
                .url("https://bbs.binmt.cc/google_translate_ips.txt")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                lastLoadIPOnlineTime = System.currentTimeMillis();
                ResponseBody body = response.body();
                if (body != null) {
                    List<String> ips = new ArrayList<>();
                    Matcher matcher = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$", Pattern.MULTILINE).matcher(body.string());
                    while (matcher.find()) {
                        ips.add(matcher.group());
                    }
                    if (!ips.isEmpty()) {
                        saveToCache(ips);
                        return ips;
                    }
                }
            }
            throw new IOException("获取可用 IP 失败");
        }
    }

    protected List<String> loadFromCache() {
        return cache;
    }

    protected void clearCache() {
        cache = null;
    }

    protected void saveToCache(List<String> ips) {
        cache = ips;
    }

}
