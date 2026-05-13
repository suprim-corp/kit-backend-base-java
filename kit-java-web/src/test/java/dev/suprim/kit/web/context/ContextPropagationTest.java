package dev.suprim.kit.web.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ContextPropagationTest {

    @AfterEach
    void tearDown() {
        RequestContext.clear();
        MDC.clear();
    }

    @Test
    void wrapRunnable_shouldRejectNull() {
        assertThrows(NullPointerException.class, () -> ContextPropagation.wrap((Runnable) null));
    }

    @Test
    void wrapCallable_shouldRejectNull() {
        assertThrows(NullPointerException.class, () -> ContextPropagation.wrap((java.util.concurrent.Callable<?>) null));
    }

    @Test
    void wrapRunnable_shouldPropagateContext() throws Exception {
        RequestContext.set(RequestContext.KEY_TRACE_ID, "trace-abc");
        MDC.put("traceId", "trace-abc");

        AtomicReference<String> capturedTraceId = new AtomicReference<>();
        AtomicReference<String> capturedMdc = new AtomicReference<>();

        Runnable wrapped = ContextPropagation.wrap(() -> {
            capturedTraceId.set(RequestContext.getTraceId().orElse("missing"));
            capturedMdc.set(MDC.get("traceId"));
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(wrapped);
        future.get();
        executor.shutdown();

        assertEquals("trace-abc", capturedTraceId.get());
        assertEquals("trace-abc", capturedMdc.get());
    }

    @Test
    void wrapCallable_shouldPropagateContext() throws Exception {
        RequestContext.set(RequestContext.KEY_TRACE_ID, "trace-xyz");
        MDC.put("traceId", "trace-xyz");

        java.util.concurrent.Callable<String> wrapped = ContextPropagation.wrap(() ->
                RequestContext.getTraceId().orElse("missing")
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(wrapped);
        String result = future.get();
        executor.shutdown();

        assertEquals("trace-xyz", result);
    }

    @Test
    void wrapRunnable_shouldCleanupAfterExecution() throws Exception {
        RequestContext.set(RequestContext.KEY_TRACE_ID, "trace-cleanup");
        MDC.put("traceId", "trace-cleanup");

        CountDownLatch taskDone = new CountDownLatch(1);
        AtomicReference<String> afterTraceId = new AtomicReference<>();
        AtomicReference<String> afterMdc = new AtomicReference<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Run wrapped task first
        Future<?> future = executor.submit(ContextPropagation.wrap(() -> {
            // context available here
        }));
        future.get();

        // Run plain task on same thread to verify cleanup
        Future<?> verifyFuture = executor.submit(() -> {
            afterTraceId.set(RequestContext.getTraceId().orElse("empty"));
            afterMdc.set(MDC.get("traceId"));
            taskDone.countDown();
        });
        verifyFuture.get();
        executor.shutdown();

        taskDone.await();
        assertEquals("empty", afterTraceId.get());
        assertNull(afterMdc.get());
    }

    @Test
    void wrapRunnable_shouldHandleNullMdcSnapshot() throws Exception {
        // MDC is empty (getCopyOfContextMap returns null for some implementations)
        MDC.clear();
        RequestContext.set(RequestContext.KEY_TRACE_ID, "trace-no-mdc");

        AtomicReference<String> capturedTraceId = new AtomicReference<>();

        Runnable wrapped = ContextPropagation.wrap(() ->
                capturedTraceId.set(RequestContext.getTraceId().orElse("missing"))
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(wrapped);
        future.get();
        executor.shutdown();

        assertEquals("trace-no-mdc", capturedTraceId.get());
    }
}
