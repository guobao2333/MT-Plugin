package guobao.plugin.translator.deeplx;

import bin.mt.plugin.api.PluginContext;

//import com.deepl.api.DeepLClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * DeepL 非官方 JSON-RPC 翻译客户端
 *
 * <p>注意：网络请求须在子线程发起，不能在主线程直接调用 translate()。
 */
public class DeepLWebTranslator implements AutoCloseable {

    // 语言代码映射
    private static final Map<String, String> LANG_MAP = Map.ofEntries(
        Map.entry("auto", "auto"),
        Map.entry("en", "EN"), Map.entry("en-US", "EN-US"), Map.entry("en-GB", "EN-GB"),
        Map.entry("zh", "ZH"), Map.entry("zh-CN", "ZH-HANS"), Map.entry("zh-TW", "ZH-HANT"),
        Map.entry("ja", "JA"), Map.entry("ru", "RU"),
        Map.entry("ko", "KO"), Map.entry("de", "DE"),
        Map.entry("fr", "FR"), Map.entry("es", "ES"),
        Map.entry("pt", "PT"), Map.entry("pt-BR", "PT-BR"), Map.entry("pt-PT", "PT-PT"),
        Map.entry("it", "IT"), Map.entry("nl", "NL"),
        Map.entry("pl", "PL"), Map.entry("ar", "AR"),
        Map.entry("tr", "TR"), Map.entry("id", "ID"),
        Map.entry("vi", "VI"), Map.entry("th", "TH"),
        Map.entry("sv", "SV"), Map.entry("da", "DA"),
        Map.entry("fi", "FI"), Map.entry("el", "EL"),
        Map.entry("cs", "CS"), Map.entry("hu", "HU"),
        Map.entry("ro", "RO"), Map.entry("nb", "NB"),
        Map.entry("uk", "UK"), Map.entry("bg", "BG"),
        Map.entry("sk", "SK"), Map.entry("sl", "SL"),
        Map.entry("lt", "LT"), Map.entry("lv", "LV"),
        Map.entry("et", "ET"), Map.entry("he", "HE"),
        Map.entry("hi", "HI")
    );

    private static final String     DEEPL_URL    = "https://www2.deepl.com/jsonrpc";
    private static final MediaType  MEDIA_JSON   = MediaType.get("application/json; charset=utf-8");

    // Records — 数据载体

    /**
     * 翻译结果。
     *
     * @param text         主翻译文本
     * @param detectedLang DeepL 检测到的源语言
     * @param alternatives 备选翻译列表（不可变）
     */
    public record TranslationResult(String text, String detectedLang, List<String> alternatives) {
        @Override
        public String toString() {
            var sb = new StringBuilder()
                    .append("翻译结果: ").append(text)
                    .append("\n检测语言: ").append(detectedLang);
            if (!alternatives.isEmpty()) {
                sb.append("\n备选翻译：");
                alternatives.forEach(alt -> sb.append("\n  - ").append(alt));
            }
            return sb.toString();
        }
    }

    /**
     * 批量翻译请求条目。
     *
     * <p>推荐用静态工厂方法构造，省去 {@code new String[]} 的噪音：
     * <pre>{@code
     *   TranslateRequest.of("Good morning", "en", "zh")
     * }</pre>
     */
    public record TranslateRequest(String text, String from, String to) {
        public static TranslateRequest of(String text, String from, String to) {
            return new TranslateRequest(text, from, to);
        }
    }

    /** 内部用，构建请求参数。 */
    private record LanguageCode(String source, String target) {}

    public sealed interface DeepLError
            permits DeepLError.UnsupportedLanguage,
                    DeepLError.EmptyInput,
                    DeepLError.NetworkFailure,
                    DeepLError.ParseFailure {

        String message();

        record UnsupportedLanguage(String langCode) implements DeepLError {
            public String message() { return "不支持的语种: " + langCode; }
        }

        record EmptyInput() implements DeepLError {
            public String message() { return "翻译文本不能为空"; }
        }

        record NetworkFailure(String detail, Throwable cause) implements DeepLError {
            public String message() { return "网络请求失败: " + detail; }
        }

        record ParseFailure(String rawResponse) implements DeepLError {
            public String message() { return "响应解析失败: " + rawResponse; }
        }
    }

    /** 携带 {@link DeepLError} 的受检异常 */
    public static final class DeepLException extends Exception {
        private final DeepLError error;

        public DeepLException(DeepLError error) {
            super(error.message(),
                    error instanceof DeepLError.NetworkFailure
                            ? ((DeepLError.NetworkFailure) error).cause()
                            : null);
            this.error = error;
        }

        public DeepLError error() { return error; }
    }


    private       PluginContext   context;
    private final OkHttpClient    httpClient;
    private final ObjectMapper    mapper;
    private final Random          random;
    /** 固定线程池 */
    private final ExecutorService executor;

    public DeepLWebTranslator() {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
    }

    public DeepLWebTranslator(PluginContext context) {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
        this.context = context;
    }

    /** 允许外部传入自定义线程池，方便与 Android 项目的线程管理集成。 */
    public DeepLWebTranslator(ExecutorService executor) {
        this.executor   = executor;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
        this.random = new Random();
    }


    /**
     * 翻译单条文本（阻塞，须在子线程调用）。
     *
     * @param text 待翻译文本
     * @param from 源语言（BCP-47 小写，"auto" 自动检测）
     * @param to   目标语言（BCP-47 小写）
     * @return 翻译结果
     * @throws DeepLException 包含具体 {@link DeepLError} 子类型
     */
    public TranslationResult translate(String text, String from, String to) throws DeepLException {
        if (text == null || text.isBlank()) {
            throw new DeepLException(new DeepLError.EmptyInput());
        }
        var langCode = resolveLanguage(from, to);

        long   id          = generateId();
        String reqBody = buildRequestBody(id, langCode, text);

        try {
            return parseResponse(post(reqBody));
        } catch (IOException e) {
            throw new DeepLException(new DeepLError.NetworkFailure(e.getMessage(), e));
        }
    }

    /**
     * 批量翻译，内部用线程池并发执行。
     *
     * <pre>{@code
     * translator.translateBatch(List.of(
     *     TranslateRequest.of("Good morning", "en", "zh"),
     *     TranslateRequest.of("Bonne nuit",   "fr", "zh")
     * ));
     * }</pre>
     *
     * @return 与输入顺序对应的结果列表（失败项为 null）
     */
    public List<TranslationResult> translateBatch(List<TranslateRequest> items) {
        List<Future<TranslationResult>> futures = items.stream()
                .map(item -> executor.submit(() -> {
                    try {
                        return translate(item.text(), item.from(), item.to());
                    } catch (DeepLException e) {
                        context.log(formatError(e.error()));
                        return null;
                    }
                }))
                .toList();

        return futures.stream()
                .map(f -> {
                    try { return f.get(); }
                    catch (Exception e) { return null; }
                })
                .toList();
    }

    /** 兼容旧的 {@code String[]} 调用方式。 */
    public List<TranslationResult> translateBatch(List<String[]> items, boolean legacy) {
        return translateBatch(items.stream()
                .map(a -> TranslateRequest.of(a[0], a[1], a[2]))
                .toList());
    }

    @Override
    public void close() {
        executor.shutdown();
        // OkHttpClient 的连接池和线程池统一清理
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    /** 将 {@link DeepLError} 格式化为日志字符串，替代 Java 21 的 pattern matching switch。 */
    private static String formatError(DeepLError error) {
        if (error instanceof DeepLError.UnsupportedLanguage) {
            return "[语言不支持] " + error.message();
        } else if (error instanceof DeepLError.EmptyInput) {
            return "[空输入] " + error.message();
        } else if (error instanceof DeepLError.NetworkFailure) {
            return "[网络错误] " + error.message();
        } else {
            return "[解析错误] " + error.message();
        }
    }

    private LanguageCode resolveLanguage(String from, String to) throws DeepLException {
        String target = LANG_MAP.get(to);
        if (target == null) throw new DeepLException(new DeepLError.UnsupportedLanguage(to));
        String source = LANG_MAP.getOrDefault(from, "ZH");
        return new LanguageCode(source, target);
    }

    private long generateId() {
        return ((long) (random.nextInt(99_999) + 100_000)) * 1_000L;
    }

    private int countI(String text) {
        int count = 0;
        for (char c : text.toCharArray()) { if (c == 'i') count++; }
        return count;
    }

    private long getTimestamp(int iCount) {
        long ts = System.currentTimeMillis();
        if (iCount != 0) {
            iCount++;
            return ts - (ts % iCount) + iCount;
        }
        return ts;
    }

    /**
     * 构造 JSON-RPC 请求体
     */
    private String buildRequestBody(long id, LanguageCode lang, String text) {
        ObjectNode root = mapper.createObjectNode();
        root.put("jsonrpc", "2.0");
        root.put("id", id);
        root.put("method", "LMT_handle_texts");

        var params   = root.putObject("params");
        params.put("splitting", "newlines");

        var langNode = params.putObject("lang");
        langNode.put("source_lang_user_selected", lang.source());
        langNode.put("target_lang", lang.target());

        var textNode = params.putArray("texts").addObject();
        textNode.put("text", text);
        textNode.put("requestAlternatives", 3);

        params.put("timestamp", getTimestamp(countI(text)));

        String json = root.toString();
        return ((id + 5) % 29 == 0 || (id + 3) % 13 == 0)
                ? json.replace("\"method\":\"", "\"method\" : \"")
                : json.replace("\"method\":\"", "\"method\": \"");
    }

    private String post(String body) throws IOException {
        var request = new Request.Builder()
                .url(DEEPL_URL)
                .post(RequestBody.create(body, MEDIA_JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + response.message());
            }
            // response.body() 非空时 string() 不返回 null
            return response.body().string();
        }
    }

    private TranslationResult parseResponse(String body) throws IOException, DeepLException {
        JsonNode result = mapper.readTree(body).path("result");
        if (result.isMissingNode()) {
            throw new DeepLException(new DeepLError.ParseFailure(body));
        }

        String   detectedLang = result.path("lang").asText();
        JsonNode firstText    = result.path("texts").elements().next();
        String   translated   = firstText.path("text").asText();

        List<String> alternatives = Arrays.stream(
                        mapper.convertValue(firstText.path("alternatives"), JsonNode[].class))
                .map(n -> n.path("text").asText())
                .toList();

        return new TranslationResult(translated, detectedLang, alternatives);
    }


    public void test() {
        // try-with-resources 自动关闭 HttpClient 与 VirtualThreadExecutor
        try (var translator = new DeepLWebTranslator()) {

            // 单条翻译
            try {
                var result = translator.translate("Hello, my friend. How are you", "en", "zh");
                context.log(result.text());
            } catch (DeepLException e) {
                context.log(formatError(e.error()));
            }

            // 批量翻译
            var results = translator.translateBatch(List.of(
                    TranslateRequest.of("Good morning",     "en", "zh"),
                    TranslateRequest.of("Bonne nuit",       "fr", "zh"),
                    TranslateRequest.of("おはようございます",  "ja", "zh")
            ));
            results.stream()
                    .filter(r -> r != null)
                    .map(TranslationResult::text)
                    .forEach(context::log);
        }
    }
}
