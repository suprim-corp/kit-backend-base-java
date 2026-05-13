package dev.suprim.kit.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ContextForwardingInterceptorTest {

    private ContextForwardingInterceptor interceptor;
    private MethodDescriptor<String, String> methodDescriptor;
    private CapturingChannel channel;

    @BeforeEach
    void setUp() {
        interceptor = new ContextForwardingInterceptor();
        methodDescriptor = MethodDescriptor.<String, String>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName("test/method")
                .setRequestMarshaller(new StringMarshaller())
                .setResponseMarshaller(new StringMarshaller())
                .build();
        channel = new CapturingChannel();
    }

    @Test
    void shouldForwardTraceIdFromContext() {
        Context context = Context.current()
                .withValue(GrpcContext.TRACE_ID, "forwarded-trace")
                .withValue(GrpcContext.REQUEST_ID, "forwarded-req");

        Context previous = context.attach();
        try {
            ClientCall<String, String> call = interceptor.interceptCall(
                    methodDescriptor, CallOptions.DEFAULT, channel);

            Metadata headers = new Metadata();
            call.start(new NoopListener<>(), headers);

            assertEquals("forwarded-trace", headers.get(MetadataUtils.TRACE_ID));
            assertEquals("forwarded-req", headers.get(MetadataUtils.REQUEST_ID));
        } finally {
            context.detach(previous);
        }
    }

    @Test
    void shouldForwardUserAndTenantFromContext() {
        Context context = Context.current()
                .withValue(GrpcContext.TRACE_ID, "trace")
                .withValue(GrpcContext.REQUEST_ID, "req")
                .withValue(GrpcContext.USER_ID, "user-123")
                .withValue(GrpcContext.TENANT_ID, "tenant-456");

        Context previous = context.attach();
        try {
            ClientCall<String, String> call = interceptor.interceptCall(
                    methodDescriptor, CallOptions.DEFAULT, channel);

            Metadata headers = new Metadata();
            call.start(new NoopListener<>(), headers);

            assertEquals("user-123", headers.get(MetadataUtils.USER_ID));
            assertEquals("tenant-456", headers.get(MetadataUtils.TENANT_ID));
        } finally {
            context.detach(previous);
        }
    }

    @Test
    void shouldNotSetHeadersWhenContextEmpty() {
        ClientCall<String, String> call = interceptor.interceptCall(
                methodDescriptor, CallOptions.DEFAULT, channel);

        Metadata headers = new Metadata();
        call.start(new NoopListener<>(), headers);

        assertNull(headers.get(MetadataUtils.TRACE_ID));
        assertNull(headers.get(MetadataUtils.REQUEST_ID));
        assertNull(headers.get(MetadataUtils.USER_ID));
        assertNull(headers.get(MetadataUtils.TENANT_ID));
    }

    private static class CapturingChannel extends Channel {
        @Override
        public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
                MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
            return new NoopClientCall<>();
        }

        @Override
        public String authority() {
            return "test-authority";
        }
    }

    private static class NoopClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {
        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            // no-op
        }

        @Override
        public void request(int numMessages) {
            // no-op
        }

        @Override
        public void cancel(String message, Throwable cause) {
            // no-op
        }

        @Override
        public void halfClose() {
            // no-op
        }

        @Override
        public void sendMessage(ReqT message) {
            // no-op
        }
    }

    private static class NoopListener<T> extends ClientCall.Listener<T> {
        // no-op
    }

    private static class StringMarshaller implements MethodDescriptor.Marshaller<String> {
        @Override
        public InputStream stream(String value) {
            return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String parse(InputStream stream) {
            try {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
