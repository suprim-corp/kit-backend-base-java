package dev.suprim.kit.grpc;

/**
 * Common gRPC constants.
 */
public final class GrpcConstants {

    private GrpcConstants() {
    }

    /**
     * Metadata key for request ID.
     */
    public static final String REQUEST_ID_KEY = "x-request-id";

    /**
     * Metadata key for trace ID.
     */
    public static final String TRACE_ID_KEY = "x-trace-id";

    /**
     * Metadata key for error code.
     */
    public static final String ERROR_CODE_KEY = "x-error-code";

    /**
     * Metadata key for error data (JSON-encoded).
     */
    public static final String ERROR_DATA_KEY = "x-error-data-bin";

    /**
     * Metadata key for user ID.
     */
    public static final String USER_ID_KEY = "x-user-id";

    /**
     * Metadata key for tenant ID.
     */
    public static final String TENANT_ID_KEY = "x-tenant-id";
}
