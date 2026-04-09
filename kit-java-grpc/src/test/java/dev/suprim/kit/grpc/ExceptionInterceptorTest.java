package dev.suprim.kit.grpc;

import dev.suprim.kit.exception.ApiException;
import dev.suprim.kit.exception.ApiRuntimeException;
import dev.suprim.kit.exception.CommonErrorCode;
import io.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionInterceptorTest {

    private ExceptionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new ExceptionInterceptor();
    }

    @Test
    void toStatusException_fromApiException_mapsCorrectly() {
        ApiException exception = new ApiException(CommonErrorCode.NOT_FOUND, "User not found");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.NOT_FOUND, result.getStatus().getCode());
        assertEquals("User not found", result.getStatus().getDescription());

        Metadata trailers = result.getTrailers();
        assertNotNull(trailers);
        assertEquals("1002", trailers.get(MetadataUtils.ERROR_CODE));
    }

    @Test
    void toStatusException_fromApiRuntimeException_mapsCorrectly() {
        ApiRuntimeException exception = new ApiRuntimeException(CommonErrorCode.UNAUTHORIZED, "Invalid token");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.UNAUTHENTICATED, result.getStatus().getCode());
        assertEquals("Invalid token", result.getStatus().getDescription());

        Metadata trailers = result.getTrailers();
        assertNotNull(trailers);
        assertEquals("1003", trailers.get(MetadataUtils.ERROR_CODE));
    }

    @Test
    void toStatusException_withData_includesDataInMetadata() {
        ApiRuntimeException exception = new ApiRuntimeException(
                CommonErrorCode.VALIDATION_ERROR,
                "Validation failed",
                "field errors here"
        );

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.INVALID_ARGUMENT, result.getStatus().getCode());

        Metadata trailers = result.getTrailers();
        assertNotNull(trailers);
        byte[] data = trailers.get(MetadataUtils.ERROR_DATA);
        assertNotNull(data);
        assertEquals("field errors here", new String(data));
    }

    @Test
    void toStatusException_withNullData_excludesDataFromMetadata() {
        ApiRuntimeException exception = new ApiRuntimeException(CommonErrorCode.NOT_FOUND, "Not found");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        Metadata trailers = result.getTrailers();
        assertNotNull(trailers);
        assertNull(trailers.get(MetadataUtils.ERROR_DATA));
    }

    @Test
    void toStatusException_forbidden_mapsToPermissionDenied() {
        ApiRuntimeException exception = ApiRuntimeException.forbidden("Access denied");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.PERMISSION_DENIED, result.getStatus().getCode());
    }

    @Test
    void toStatusException_conflict_mapsToAlreadyExists() {
        ApiRuntimeException exception = ApiRuntimeException.conflict("Resource already exists");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.ALREADY_EXISTS, result.getStatus().getCode());
    }

    @Test
    void toStatusException_serviceUnavailable_mapsToUnavailable() {
        ApiRuntimeException exception = ApiRuntimeException.serviceUnavailable("Service down");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.UNAVAILABLE, result.getStatus().getCode());
    }

    @Test
    void toStatusException_badRequest_mapsToInvalidArgument() {
        ApiRuntimeException exception = ApiRuntimeException.badRequest("Invalid input");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.INVALID_ARGUMENT, result.getStatus().getCode());
    }

    @Test
    void toStatusException_internalError_mapsToInternal() {
        ApiRuntimeException exception = ApiRuntimeException.internalError("Something went wrong");

        StatusRuntimeException result = ExceptionInterceptor.toStatusException(exception);

        assertEquals(Status.Code.INTERNAL, result.getStatus().getCode());
    }

    @Test
    void interceptCall_returnsListener() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus);
        Metadata headers = new Metadata();
        ServerCall.Listener<String> delegateListener = new ServerCall.Listener<>() {};
        ServerCallHandler<String, String> next = (c, h) -> delegateListener;

        ServerCall.Listener<String> result = interceptor.interceptCall(call, headers, next);

        assertNotNull(result);
    }

    @Test
    void interceptCall_onMessage_successfulCompletion() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        AtomicReference<String> receivedMessage = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> delegateListener = new ServerCall.Listener<>() {
            @Override
            public void onMessage(String message) {
                receivedMessage.set(message);
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> delegateListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onMessage("test message");

        assertEquals("test message", receivedMessage.get());
        assertNull(closedStatus.get()); // No exception, so call not closed
    }

    @Test
    void interceptCall_onHalfClose_successfulCompletion() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        AtomicReference<Boolean> halfCloseCalled = new AtomicReference<>(false);
        TestServerCall call = new TestServerCall(closedStatus);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> delegateListener = new ServerCall.Listener<>() {
            @Override
            public void onHalfClose() {
                halfCloseCalled.set(true);
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> delegateListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onHalfClose();

        assertTrue(halfCloseCalled.get());
        assertNull(closedStatus.get()); // No exception, so call not closed
    }

    @Test
    void interceptCall_onHalfClose_catchesApiRuntimeException() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        AtomicReference<Metadata> closedMetadata = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus, closedMetadata);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> throwingListener = new ServerCall.Listener<>() {
            @Override
            public void onHalfClose() {
                throw ApiRuntimeException.notFound("Not found");
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> throwingListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onHalfClose();

        assertEquals(Status.Code.NOT_FOUND, closedStatus.get().getCode());
        assertEquals("Not found", closedStatus.get().getDescription());
    }

    @Test
    void interceptCall_onHalfClose_catchesWrappedApiException() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus);
        Metadata headers = new Metadata();

        ApiException apiException = new ApiException(CommonErrorCode.FORBIDDEN, "Forbidden");
        ServerCall.Listener<String> throwingListener = new ServerCall.Listener<>() {
            @Override
            public void onHalfClose() {
                throw new RuntimeException("Wrapped", apiException);
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> throwingListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onHalfClose();

        assertEquals(Status.Code.PERMISSION_DENIED, closedStatus.get().getCode());
    }

    @Test
    void interceptCall_onHalfClose_rethrowsNonApiException() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> throwingListener = new ServerCall.Listener<>() {
            @Override
            public void onHalfClose() {
                throw new IllegalArgumentException("Not an API exception");
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> throwingListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);

        assertThrows(IllegalArgumentException.class, listener::onHalfClose);
    }

    @Test
    void interceptCall_onMessage_catchesApiRuntimeException() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> throwingListener = new ServerCall.Listener<>() {
            @Override
            public void onMessage(String message) {
                throw ApiRuntimeException.badRequest("Bad request");
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> throwingListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onMessage("test");

        assertEquals(Status.Code.INVALID_ARGUMENT, closedStatus.get().getCode());
    }

    @Test
    void interceptCall_onHalfClose_includesErrorDataInMetadata() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        AtomicReference<Metadata> closedMetadata = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus, closedMetadata);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> throwingListener = new ServerCall.Listener<>() {
            @Override
            public void onHalfClose() {
                throw new ApiRuntimeException(
                        CommonErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        "error data"
                );
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> throwingListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onHalfClose();

        assertEquals("1006", closedMetadata.get().get(MetadataUtils.ERROR_CODE));
        assertArrayEquals("error data".getBytes(), closedMetadata.get().get(MetadataUtils.ERROR_DATA));
    }

    @Test
    void interceptCall_onHalfClose_handlesNullData() {
        AtomicReference<Status> closedStatus = new AtomicReference<>();
        AtomicReference<Metadata> closedMetadata = new AtomicReference<>();
        TestServerCall call = new TestServerCall(closedStatus, closedMetadata);
        Metadata headers = new Metadata();

        ServerCall.Listener<String> throwingListener = new ServerCall.Listener<>() {
            @Override
            public void onHalfClose() {
                throw ApiRuntimeException.notFound("Not found");
            }
        };
        ServerCallHandler<String, String> next = (c, h) -> throwingListener;

        ServerCall.Listener<String> listener = interceptor.interceptCall(call, headers, next);
        listener.onHalfClose();

        assertNotNull(closedMetadata.get());
        assertNull(closedMetadata.get().get(MetadataUtils.ERROR_DATA));
    }

    /**
     * Test implementation of ServerCall that captures close() arguments.
     */
    private static class TestServerCall extends ServerCall<String, String> {
        private final AtomicReference<Status> closedStatus;
        private final AtomicReference<Metadata> closedMetadata;

        TestServerCall(AtomicReference<Status> closedStatus) {
            this(closedStatus, new AtomicReference<>());
        }

        TestServerCall(AtomicReference<Status> closedStatus, AtomicReference<Metadata> closedMetadata) {
            this.closedStatus = closedStatus;
            this.closedMetadata = closedMetadata;
        }

        @Override
        public void request(int numMessages) {}

        @Override
        public void sendHeaders(Metadata headers) {}

        @Override
        public void sendMessage(String message) {}

        @Override
        public void close(Status status, Metadata trailers) {
            closedStatus.set(status);
            closedMetadata.set(trailers);
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public MethodDescriptor<String, String> getMethodDescriptor() {
            return null;
        }
    }
}
