package dev.suprim.kit.web.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds request-scoped context (trace ID, request ID, etc.) via ThreadLocal.
 * <p>
 * Lifecycle is managed by {@link RequestContextFilter} for HTTP requests.
 * For gRPC, use the corresponding interceptor in kit-java-grpc.
 * <p>
 * Usage:
 * <pre>
 *   String traceId = RequestContext.getTraceId().orElse("unknown");
 * </pre>
 */
public final class RequestContext {

    public static final String KEY_TRACE_ID = "traceId";
    public static final String KEY_REQUEST_ID = "requestId";

    private static final ThreadLocal<Map<String, String>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    private RequestContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sets a value in the current request context.
     *
     * @param key   context key, must not be null
     * @param value context value, must not be null
     */
    public static void set(String key, String value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        CONTEXT.get().put(key, value);
    }

    /**
     * Gets a value from the current request context.
     */
    public static Optional<String> get(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(CONTEXT.get().get(key));
    }

    /**
     * Gets the trace ID for the current request.
     */
    public static Optional<String> getTraceId() {
        return get(KEY_TRACE_ID);
    }

    /**
     * Gets the request ID for the current request.
     */
    public static Optional<String> getRequestId() {
        return get(KEY_REQUEST_ID);
    }

    /**
     * Returns an unmodifiable snapshot of the current context.
     * Useful for propagating context to async tasks.
     */
    public static Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(CONTEXT.get()));
    }

    /**
     * Restores a previously captured context snapshot.
     * Typically used in async task execution.
     *
     * @param contextSnapshot snapshot to restore, must not be null
     */
    public static void restore(Map<String, String> contextSnapshot) {
        Objects.requireNonNull(contextSnapshot, "contextSnapshot");
        clear();
        CONTEXT.get().putAll(contextSnapshot);
    }

    /**
     * Clears the current request context. Must be called at the end of request processing
     * to prevent memory leaks.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
