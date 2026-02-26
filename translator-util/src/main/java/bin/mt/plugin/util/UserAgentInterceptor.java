package bin.mt.plugin.util;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {
    public static final Interceptor INSTANCE = new UserAgentInterceptor();

    private UserAgentInterceptor() {
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String ua = System.getProperty("http.agent");
        if (ua == null) {
            ua = "Mozilla/5.0 (Linux; Android 8.0;)";
        }
        Request request = chain.request().newBuilder()
                .header("User-Agent", ua)
                .build();
        return chain.proceed(request);
    }
}
