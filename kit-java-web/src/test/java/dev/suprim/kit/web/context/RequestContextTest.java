package dev.suprim.kit.web.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestContextTest {

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    @Test
    void set_shouldStoreValue() {
        RequestContext.set("key", "value");

        assertEquals("value", RequestContext.get("key").orElseThrow());
    }

    @Test
    void set_shouldRejectNullKey() {
        assertThrows(NullPointerException.class, () -> RequestContext.set(null, "value"));
    }

    @Test
    void set_shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> RequestContext.set("key", null));
    }

    @Test
    void get_shouldReturnEmptyForMissingKey() {
        assertTrue(RequestContext.get("nonexistent").isEmpty());
    }

    @Test
    void get_shouldRejectNullKey() {
        assertThrows(NullPointerException.class, () -> RequestContext.get(null));
    }

    @Test
    void getTraceId_shouldReturnStoredTraceId() {
        RequestContext.set(RequestContext.KEY_TRACE_ID, "trace-123");

        assertEquals("trace-123", RequestContext.getTraceId().orElseThrow());
    }

    @Test
    void getRequestId_shouldReturnStoredRequestId() {
        RequestContext.set(RequestContext.KEY_REQUEST_ID, "req-456");

        assertEquals("req-456", RequestContext.getRequestId().orElseThrow());
    }

    @Test
    void snapshot_shouldReturnUnmodifiableCopy() {
        RequestContext.set("key1", "val1");
        RequestContext.set("key2", "val2");

        Map<String, String> snapshot = RequestContext.snapshot();

        assertEquals(2, snapshot.size());
        assertEquals("val1", snapshot.get("key1"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("key3", "val3"));
    }

    @Test
    void snapshot_shouldBeIndependentFromOriginal() {
        RequestContext.set("key1", "val1");
        Map<String, String> snapshot = RequestContext.snapshot();

        RequestContext.set("key2", "val2");

        assertFalse(snapshot.containsKey("key2"));
    }

    @Test
    void restore_shouldReplaceCurrentContext() {
        RequestContext.set("old", "value");

        RequestContext.restore(Map.of("new", "restored"));

        assertTrue(RequestContext.get("old").isEmpty());
        assertEquals("restored", RequestContext.get("new").orElseThrow());
    }

    @Test
    void restore_shouldRejectNull() {
        assertThrows(NullPointerException.class, () -> RequestContext.restore(null));
    }

    @Test
    void clear_shouldRemoveAllValues() {
        RequestContext.set("key1", "val1");
        RequestContext.set("key2", "val2");

        RequestContext.clear();

        assertTrue(RequestContext.get("key1").isEmpty());
        assertTrue(RequestContext.get("key2").isEmpty());
    }

    @Test
    void context_shouldBeThreadIsolated() throws InterruptedException {
        RequestContext.set("main", "mainValue");

        Thread otherThread = new Thread(() -> {
            assertTrue(RequestContext.get("main").isEmpty());
            RequestContext.set("other", "otherValue");
        });
        otherThread.start();
        otherThread.join();

        assertTrue(RequestContext.get("other").isEmpty());
        assertEquals("mainValue", RequestContext.get("main").orElseThrow());
    }

    @Test
    void constructor_shouldThrowUnsupportedOperationException() throws Exception {
        Constructor<RequestContext> constructor = RequestContext.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }
}
