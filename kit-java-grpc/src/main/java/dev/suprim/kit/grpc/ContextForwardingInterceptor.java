package dev.suprim.kit.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.Optional;

/**
 * gRPC client interceptor that forwards trace/request IDs from the current gRPC {@link io.grpc.Context}
 * to outgoing call metadata.
 * <p>
 * This ensures trace context is propagated across service-to-service gRPC calls.
 * <p>
 * Registration example:
 * <pre>
 * ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090")
 *     .intercept(new ContextForwardingInterceptor())
 *     .build();
 * </pre>
 */
public class ContextForwardingInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                propagateContextToHeaders(headers);
                super.start(responseListener, headers);
            }
        };
    }

    private void propagateContextToHeaders(Metadata headers) {
        Optional.ofNullable(GrpcContext.getTraceId())
                .ifPresent(value -> headers.put(MetadataUtils.TRACE_ID, value));

        Optional.ofNullable(GrpcContext.getRequestId())
                .ifPresent(value -> headers.put(MetadataUtils.REQUEST_ID, value));

        Optional.ofNullable(GrpcContext.getUserId())
                .ifPresent(value -> headers.put(MetadataUtils.USER_ID, value));

        Optional.ofNullable(GrpcContext.getTenantId())
                .ifPresent(value -> headers.put(MetadataUtils.TENANT_ID, value));
    }
}
