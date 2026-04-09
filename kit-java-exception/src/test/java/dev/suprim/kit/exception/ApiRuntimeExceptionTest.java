package dev.suprim.kit.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiRuntimeExceptionTest {

    @Test
    void shouldExtendRuntimeException() {
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.NOT_FOUND);
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void constructor_withErrorCode_shouldSetProperties() {
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.NOT_FOUND);
        assertEquals(1002, ex.getCode());
        assertEquals("Resource not found", ex.getMessage());
        assertEquals(404, ex.getHttpStatus());
        assertNull(ex.getData());
    }

    @Test
    void constructor_withCustomMessage_shouldOverrideMessage() {
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.NOT_FOUND, "User not found");
        assertEquals(1002, ex.getCode());
        assertEquals("User not found", ex.getMessage());
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void constructor_withData_shouldStoreData() {
        Map<String, String> data = Map.of("field", "email");
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.VALIDATION_ERROR, data);
        assertEquals(data, ex.getData());
        assertTrue(ex.hasData());
    }

    @Test
    void constructor_withCause_shouldChainException() {
        RuntimeException cause = new RuntimeException("Original error");
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.UNKNOWN_ERROR, cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void builder_shouldCreateException() {
        ApiRuntimeException ex = ApiRuntimeException.builder()
            .errorCode(CommonErrorCode.FORBIDDEN)
            .message("Access denied")
            .data(Map.of("resource", "admin"))
            .build();

        assertEquals(1004, ex.getCode());
        assertEquals("Access denied", ex.getMessage());
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void staticFactoryMethods_shouldCreateCorrectExceptions() {
        assertEquals(404, ApiRuntimeException.notFound("test").getHttpStatus());
        assertEquals(400, ApiRuntimeException.badRequest("test").getHttpStatus());
        assertEquals(401, ApiRuntimeException.unauthorized("test").getHttpStatus());
        assertEquals(403, ApiRuntimeException.forbidden("test").getHttpStatus());
        assertEquals(409, ApiRuntimeException.conflict("test").getHttpStatus());
        assertEquals(503, ApiRuntimeException.serviceUnavailable("test").getHttpStatus());
        assertEquals(500, ApiRuntimeException.internalError("test").getHttpStatus());
    }

    @Test
    void validationError_shouldIncludeData() {
        Map<String, String> errors = Map.of("email", "invalid");
        ApiRuntimeException ex = ApiRuntimeException.validationError("Validation failed", errors);
        assertEquals(errors, ex.getData());
    }

    // Additional constructor tests
    @Test
    void constructor_withMessageAndData_shouldSetBoth() {
        Map<String, Object> data = Map.of("key", "value");
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.VALIDATION_ERROR, "Custom message", data);
        assertEquals("Custom message", ex.getMessage());
        assertEquals(data, ex.getData());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        Throwable cause = new RuntimeException("root cause");
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.UNKNOWN_ERROR, "Custom message", cause);
        assertEquals("Custom message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void constructor_withCodeAndMessage_shouldSetDefaults() {
        ApiRuntimeException ex = new ApiRuntimeException(9999, "Custom error");
        assertEquals(9999, ex.getCode());
        assertEquals("Custom error", ex.getMessage());
        assertEquals(500, ex.getHttpStatus()); // default
    }

    @Test
    void constructor_withCodeMessageAndHttpStatus_shouldSetAll() {
        ApiRuntimeException ex = new ApiRuntimeException(8888, "Custom error", 418);
        assertEquals(8888, ex.getCode());
        assertEquals("Custom error", ex.getMessage());
        assertEquals(418, ex.getHttpStatus());
    }

    // Builder additional methods
    @Test
    void builder_withCodeAndHttpStatus_shouldSet() {
        ApiRuntimeException ex = ApiRuntimeException.builder()
            .code(7777)
            .message("Builder error")
            .httpStatus(422)
            .build();
        assertEquals(7777, ex.getCode());
        assertEquals(422, ex.getHttpStatus());
    }

    @Test
    void builder_withCause_shouldSetCause() {
        Throwable cause = new IllegalStateException("state error");
        ApiRuntimeException ex = ApiRuntimeException.builder()
            .errorCode(CommonErrorCode.UNKNOWN_ERROR)
            .cause(cause)
            .build();
        assertEquals(cause, ex.getCause());
    }

    @Test
    void internalError_withCause_shouldSetCause() {
        Throwable cause = new RuntimeException("internal");
        ApiRuntimeException ex = ApiRuntimeException.internalError("Error occurred", cause);
        assertEquals(cause, ex.getCause());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    void hasData_shouldReturnFalseWhenNoData() {
        ApiRuntimeException ex = new ApiRuntimeException(CommonErrorCode.NOT_FOUND);
        assertFalse(ex.hasData());
    }

    // Custom StatusCode test for default getHttpStatus()
    @Test
    void customStatusCode_shouldUseDefaultHttpStatus() {
        StatusCode customCode = new StatusCode() {
            @Override
            public int getCode() {
                return 12345;
            }

            @Override
            public String getMessage() {
                return "Custom status";
            }
            // Not overriding getHttpStatus() - should use default 200
        };

        ApiRuntimeException ex = new ApiRuntimeException(customCode);
        assertEquals(12345, ex.getCode());
        assertEquals("Custom status", ex.getMessage());
        assertEquals(200, ex.getHttpStatus()); // default from StatusCode interface
    }
}
