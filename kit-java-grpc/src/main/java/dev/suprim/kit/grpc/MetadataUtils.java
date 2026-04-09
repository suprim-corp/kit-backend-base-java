package dev.suprim.kit.grpc;

import io.grpc.Metadata;

import static dev.suprim.kit.grpc.GrpcConstants.*;

/**
 * Utility class for working with gRPC metadata.
 */
public final class MetadataUtils {

    private MetadataUtils() {
    }

    /**
     * Metadata key for request ID.
     */
    public static final Metadata.Key<String> REQUEST_ID =
            Metadata.Key.of(REQUEST_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Metadata key for trace ID.
     */
    public static final Metadata.Key<String> TRACE_ID =
            Metadata.Key.of(TRACE_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Metadata key for error code.
     */
    public static final Metadata.Key<String> ERROR_CODE =
            Metadata.Key.of(ERROR_CODE_KEY, Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Metadata key for error data (binary).
     */
    public static final Metadata.Key<byte[]> ERROR_DATA =
            Metadata.Key.of(ERROR_DATA_KEY, Metadata.BINARY_BYTE_MARSHALLER);

    /**
     * Metadata key for user ID.
     */
    public static final Metadata.Key<String> USER_ID =
            Metadata.Key.of(USER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Metadata key for tenant ID.
     */
    public static final Metadata.Key<String> TENANT_ID =
            Metadata.Key.of(TENANT_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Gets a string value from metadata, or null if not present.
     */
    public static String getString(Metadata metadata, Metadata.Key<String> key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Gets a string value from metadata with a default value.
     */
    public static String getString(Metadata metadata, Metadata.Key<String> key, String defaultValue) {
        String value = getString(metadata, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Creates metadata with request ID.
     */
    public static Metadata withRequestId(String requestId) {
        Metadata metadata = new Metadata();
        metadata.put(REQUEST_ID, requestId);
        return metadata;
    }

    /**
     * Creates metadata with error details.
     */
    public static Metadata withError(int code, byte[] data) {
        Metadata metadata = new Metadata();
        metadata.put(ERROR_CODE, String.valueOf(code));
        if (data != null) {
            metadata.put(ERROR_DATA, data);
        }
        return metadata;
    }
}
