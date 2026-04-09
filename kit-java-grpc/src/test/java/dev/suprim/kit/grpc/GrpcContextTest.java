package dev.suprim.kit.grpc;

import io.grpc.Context;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcContextTest {

    @Test
    void getRequestId_returnsNullWhenNotSet() {
        assertNull(GrpcContext.getRequestId());
    }

    @Test
    void getTraceId_returnsNullWhenNotSet() {
        assertNull(GrpcContext.getTraceId());
    }

    @Test
    void getUserId_returnsNullWhenNotSet() {
        assertNull(GrpcContext.getUserId());
    }

    @Test
    void getTenantId_returnsNullWhenNotSet() {
        assertNull(GrpcContext.getTenantId());
    }

    @Test
    void getRequestId_returnsValueWhenSet() {
        Context ctx = Context.current().withValue(GrpcContext.REQUEST_ID, "req-123");
        ctx.run(() -> assertEquals("req-123", GrpcContext.getRequestId()));
    }

    @Test
    void getTraceId_returnsValueWhenSet() {
        Context ctx = Context.current().withValue(GrpcContext.TRACE_ID, "trace-456");
        ctx.run(() -> assertEquals("trace-456", GrpcContext.getTraceId()));
    }

    @Test
    void getUserId_returnsValueWhenSet() {
        Context ctx = Context.current().withValue(GrpcContext.USER_ID, "user-789");
        ctx.run(() -> assertEquals("user-789", GrpcContext.getUserId()));
    }

    @Test
    void getTenantId_returnsValueWhenSet() {
        Context ctx = Context.current().withValue(GrpcContext.TENANT_ID, "tenant-abc");
        ctx.run(() -> assertEquals("tenant-abc", GrpcContext.getTenantId()));
    }

    @Test
    void fromMetadata_extractsAllValues() {
        Metadata metadata = new Metadata();
        metadata.put(MetadataUtils.REQUEST_ID, "req-123");
        metadata.put(MetadataUtils.TRACE_ID, "trace-456");
        metadata.put(MetadataUtils.USER_ID, "user-789");
        metadata.put(MetadataUtils.TENANT_ID, "tenant-abc");

        Context ctx = GrpcContext.fromMetadata(metadata);

        ctx.run(() -> {
            assertEquals("req-123", GrpcContext.getRequestId());
            assertEquals("trace-456", GrpcContext.getTraceId());
            assertEquals("user-789", GrpcContext.getUserId());
            assertEquals("tenant-abc", GrpcContext.getTenantId());
        });
    }

    @Test
    void fromMetadata_handlesPartialMetadata() {
        Metadata metadata = new Metadata();
        metadata.put(MetadataUtils.REQUEST_ID, "req-only");

        Context ctx = GrpcContext.fromMetadata(metadata);

        ctx.run(() -> {
            assertEquals("req-only", GrpcContext.getRequestId());
            assertNull(GrpcContext.getTraceId());
            assertNull(GrpcContext.getUserId());
            assertNull(GrpcContext.getTenantId());
        });
    }

    @Test
    void fromMetadata_handlesEmptyMetadata() {
        Metadata metadata = new Metadata();

        Context ctx = GrpcContext.fromMetadata(metadata);

        ctx.run(() -> {
            assertNull(GrpcContext.getRequestId());
            assertNull(GrpcContext.getTraceId());
            assertNull(GrpcContext.getUserId());
            assertNull(GrpcContext.getTenantId());
        });
    }

    @Test
    void contextKeys_areNotNull() {
        assertNotNull(GrpcContext.REQUEST_ID);
        assertNotNull(GrpcContext.TRACE_ID);
        assertNotNull(GrpcContext.USER_ID);
        assertNotNull(GrpcContext.TENANT_ID);
    }
}
