package dev.suprim.kit.grpc;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContextPropagationInterceptorTest {

    private ContextPropagationInterceptor interceptor;

    @Mock
    private ServerCall<Object, Object> call;

    @Mock
    private ServerCallHandler<Object, Object> next;

    @Mock
    private ServerCall.Listener<Object> listener;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        interceptor = new ContextPropagationInterceptor();
        when(next.startCall(any(), any())).thenReturn(listener);
    }

    @AfterEach
    void tearDown() throws Exception {
        MDC.clear();
        mocks.close();
    }

    @Test
    void shouldExtractTraceIdFromMetadata() {
        Metadata headers = new Metadata();
        headers.put(MetadataUtils.TRACE_ID, "grpc-trace-123");
        headers.put(MetadataUtils.REQUEST_ID, "grpc-req-456");

        interceptor.interceptCall(call, headers, next);

        assertEquals("grpc-trace-123", MDC.get(ContextPropagationInterceptor.MDC_TRACE_ID));
        assertEquals("grpc-req-456", MDC.get(ContextPropagationInterceptor.MDC_REQUEST_ID));
    }

    @Test
    void shouldGenerateIdsWhenMetadataAbsent() {
        Metadata headers = new Metadata();

        interceptor.interceptCall(call, headers, next);

        String traceId = MDC.get(ContextPropagationInterceptor.MDC_TRACE_ID);
        String requestId = MDC.get(ContextPropagationInterceptor.MDC_REQUEST_ID);

        assertNotNull(traceId);
        assertNotNull(requestId);
        assertFalse(traceId.isBlank());
        assertFalse(requestId.isBlank());
        assertNotEquals(traceId, requestId);
    }

    @Test
    void shouldGenerateIdsWhenMetadataBlank() {
        Metadata headers = new Metadata();
        headers.put(MetadataUtils.TRACE_ID, "   ");
        headers.put(MetadataUtils.REQUEST_ID, "");

        interceptor.interceptCall(call, headers, next);

        String traceId = MDC.get(ContextPropagationInterceptor.MDC_TRACE_ID);
        assertNotNull(traceId);
        assertNotEquals("   ", traceId);
    }

    @Test
    void shouldAttachUserAndTenantToContext() {
        Metadata headers = new Metadata();
        headers.put(MetadataUtils.TRACE_ID, "trace");
        headers.put(MetadataUtils.REQUEST_ID, "req");
        headers.put(MetadataUtils.USER_ID, "user-789");
        headers.put(MetadataUtils.TENANT_ID, "tenant-abc");

        // Capture the context that next.startCall receives
        when(next.startCall(any(), any())).thenAnswer(invocation -> {
            assertEquals("user-789", GrpcContext.USER_ID.get());
            assertEquals("tenant-abc", GrpcContext.TENANT_ID.get());
            assertEquals("trace", GrpcContext.TRACE_ID.get());
            assertEquals("req", GrpcContext.REQUEST_ID.get());
            return listener;
        });

        interceptor.interceptCall(call, headers, next);
    }

    @Test
    void shouldRejectNullHeaders() {
        assertThrows(NullPointerException.class, () -> interceptor.interceptCall(call, null, next));
    }
}
