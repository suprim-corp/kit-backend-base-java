package dev.suprim.kit.web.context;

import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Utilities for propagating {@link RequestContext} and MDC across async boundaries.
 * <p>
 * When spawning async tasks (thread pools, CompletableFuture, etc.), the trace context
 * is lost because it lives in ThreadLocal. These wrappers capture and restore it.
 * <p>
 * Usage:
 * <pre>
 *   executor.submit(ContextPropagation.wrap(() -&gt; {
 *       // RequestContext and MDC are available here
 *       String traceId = RequestContext.getTraceId().orElse("unknown");
 *   }));
 * </pre>
 */
public final class ContextPropagation {

    private ContextPropagation() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Wraps a Runnable to propagate the current RequestContext and MDC to the executing thread.
     *
     * @param task the task to wrap, must not be null
     * @return a context-aware Runnable
     */
    public static Runnable wrap(Runnable task) {
        Objects.requireNonNull(task, "task");
        Map<String, String> contextSnapshot = RequestContext.snapshot();
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        return () -> {
            RequestContext.restore(contextSnapshot);
            setMdcContext(mdcSnapshot);
            try {
                task.run();
            } finally {
                RequestContext.clear();
                MDC.clear();
            }
        };
    }

    /**
     * Wraps a Callable to propagate the current RequestContext and MDC to the executing thread.
     *
     * @param task the task to wrap, must not be null
     * @return a context-aware Callable
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        Objects.requireNonNull(task, "task");
        Map<String, String> contextSnapshot = RequestContext.snapshot();
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        return () -> {
            RequestContext.restore(contextSnapshot);
            setMdcContext(mdcSnapshot);
            try {
                return task.call();
            } finally {
                RequestContext.clear();
                MDC.clear();
            }
        };
    }

    private static void setMdcContext(Map<String, String> mdcSnapshot) {
        if (mdcSnapshot != null) {
            MDC.setContextMap(mdcSnapshot);
        } else {
            MDC.clear();
        }
    }
}
