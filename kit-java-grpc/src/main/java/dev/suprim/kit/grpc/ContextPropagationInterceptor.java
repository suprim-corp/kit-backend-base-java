package dev.suprim.kit.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * gRPC server interceptor that extracts trace/request IDs from incoming metadata,
 * attaches them to the gRPC {@link Context}, and sets SLF4J MDC for log correlation.
 * <p>
 * If trace ID or request ID is absent in metadata, a new UUID v4 is generated.
 * <p>
 * Registration example:
 * <pre>
 * Server server = ServerBuilder.forPort(9090)
 *     .addService(new MyServiceImpl())
 *     .intercept(new ContextPropagationInterceptor())
 *     .intercept(new ExceptionInterceptor())
 *     .build();
 * </pre>
 */
public class ContextPropagationInterceptor implements ServerInterceptor {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        Objects.requireNonNull(headers, "headers");

        String traceId = resolveFromMetadata(headers, MetadataUtils.TRACE_ID);
        String requestId = resolveFromMetadata(headers, MetadataUtils.REQUEST_ID);

        Context context = Context.current()
                .withValue(GrpcContext.TRACE_ID, traceId)
                .withValue(GrpcContext.REQUEST_ID, requestId);

        // Also extract user/tenant if present
        context = attachIfPresent(context, headers, MetadataUtils.USER_ID, GrpcContext.USER_ID);
        context = attachIfPresent(context, headers, MetadataUtils.TENANT_ID, GrpcContext.TENANT_ID);

        // Set MDC for log correlation within this call
        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_REQUEST_ID, requestId);

        return new MdcCleanupListener<>(Contexts.interceptCall(context, call, headers, next));
    }

    private String resolveFromMetadata(Metadata headers, Metadata.Key<String> key) {
        return Optional.ofNullable(MetadataUtils.getString(headers, key))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private Context attachIfPresent(Context context, Metadata headers,
                                    Metadata.Key<String> metadataKey, Context.Key<String> contextKey) {
        return Optional.ofNullable(MetadataUtils.getString(headers, metadataKey))
                .map(value -> context.withValue(contextKey, value))
                .orElse(context);
    }

    /**
     * Listener wrapper that cleans up MDC when the call completes or is cancelled.
     */
    private static class MdcCleanupListener<ReqT>
            extends io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {

        MdcCleanupListener(ServerCall.Listener<ReqT> delegate) {
            super(delegate);
        }

        @Override
        public void onComplete() {
            try {
                super.onComplete();
            } finally {
                clearMdc();
            }
        }

        @Override
        public void onCancel() {
            try {
                super.onCancel();
            } finally {
                clearMdc();
            }
        }

        private static void clearMdc() {
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
