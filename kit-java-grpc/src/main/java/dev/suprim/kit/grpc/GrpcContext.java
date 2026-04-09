package dev.suprim.kit.grpc;

import io.grpc.Context;
import io.grpc.Metadata;

/**
 * Context keys for gRPC request propagation.
 */
public final class GrpcContext {

    private GrpcContext() {
    }

    /**
     * Context key for request ID.
     */
    public static final Context.Key<String> REQUEST_ID = Context.key("request-id");

    /**
     * Context key for trace ID.
     */
    public static final Context.Key<String> TRACE_ID = Context.key("trace-id");

    /**
     * Context key for user ID.
     */
    public static final Context.Key<String> USER_ID = Context.key("user-id");

    /**
     * Context key for tenant ID.
     */
    public static final Context.Key<String> TENANT_ID = Context.key("tenant-id");

    /**
     * Gets the current request ID from context, or null if not set.
     */
    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    /**
     * Gets the current trace ID from context, or null if not set.
     */
    public static String getTraceId() {
        return TRACE_ID.get();
    }

    /**
     * Gets the current user ID from context, or null if not set.
     */
    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * Gets the current tenant ID from context, or null if not set.
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * Creates a new context with values extracted from metadata.
     */
    public static Context fromMetadata(Metadata metadata) {
        Context ctx = Context.current();

        String requestId = MetadataUtils.getString(metadata, MetadataUtils.REQUEST_ID);
        if (requestId != null) {
            ctx = ctx.withValue(REQUEST_ID, requestId);
        }

        String traceId = MetadataUtils.getString(metadata, MetadataUtils.TRACE_ID);
        if (traceId != null) {
            ctx = ctx.withValue(TRACE_ID, traceId);
        }

        String userId = MetadataUtils.getString(metadata, MetadataUtils.USER_ID);
        if (userId != null) {
            ctx = ctx.withValue(USER_ID, userId);
        }

        String tenantId = MetadataUtils.getString(metadata, MetadataUtils.TENANT_ID);
        if (tenantId != null) {
            ctx = ctx.withValue(TENANT_ID, tenantId);
        }

        return ctx;
    }
}
