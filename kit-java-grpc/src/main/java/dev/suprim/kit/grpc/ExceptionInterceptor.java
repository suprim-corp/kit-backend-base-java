package dev.suprim.kit.grpc;

import dev.suprim.kit.exception.ApiException;
import dev.suprim.kit.exception.ApiRuntimeException;
import io.grpc.*;

/**
 * Server interceptor that catches ApiException and ApiRuntimeException
 * and converts them to gRPC StatusException with error details in metadata.
 */
public class ExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (ApiRuntimeException e) {
                    closeWithException(call, e.getHttpStatus(), e.getCode(), e.getMessage(), e.getData());
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof ApiException apiEx) {
                        closeWithException(call, apiEx.getHttpStatus(), apiEx.getCode(), apiEx.getMessage(), apiEx.getData());
                    } else {
                        throw e;
                    }
                }
            }

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (ApiRuntimeException e) {
                    closeWithException(call, e.getHttpStatus(), e.getCode(), e.getMessage(), e.getData());
                }
            }
        };
    }

    private <ReqT, RespT> void closeWithException(
            ServerCall<ReqT, RespT> call,
            int httpStatus,
            int code,
            String message,
            Object data) {

        Status grpcStatus = GrpcStatusMapper.fromHttpStatus(httpStatus)
                .withDescription(message);

        Metadata trailers = MetadataUtils.withError(code, serializeData(data));
        call.close(grpcStatus, trailers);
    }

    private byte[] serializeData(Object data) {
        if (data == null) {
            return null;
        }
        // Simple serialization - in production, use proper JSON serialization
        return data.toString().getBytes();
    }

    /**
     * Creates a StatusRuntimeException from an ApiException.
     * Useful for manually throwing in service implementations.
     */
    public static StatusRuntimeException toStatusException(ApiException e) {
        Status status = GrpcStatusMapper.fromHttpStatus(e.getHttpStatus())
                .withDescription(e.getMessage());
        Metadata metadata = MetadataUtils.withError(e.getCode(), serializeDataStatic(e.getData()));
        return status.asRuntimeException(metadata);
    }

    /**
     * Creates a StatusRuntimeException from an ApiRuntimeException.
     * Useful for manually throwing in service implementations.
     */
    public static StatusRuntimeException toStatusException(ApiRuntimeException e) {
        Status status = GrpcStatusMapper.fromHttpStatus(e.getHttpStatus())
                .withDescription(e.getMessage());
        Metadata metadata = MetadataUtils.withError(e.getCode(), serializeDataStatic(e.getData()));
        return status.asRuntimeException(metadata);
    }

    private static byte[] serializeDataStatic(Object data) {
        if (data == null) {
            return null;
        }
        return data.toString().getBytes();
    }
}
