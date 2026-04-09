package dev.suprim.kit.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void constructor_withErrorCode_shouldSetProperties() {
        ApiException ex = new ApiException(CommonErrorCode.NOT_FOUND);
        assertEquals(1002, ex.getCode());
        assertEquals("Resource not found", ex.getMessage());
        assertEquals(404, ex.getHttpStatus());
        assertNull(ex.getData());
    }

    @Test
    void constructor_withCustomMessage_shouldOverrideMessage() {
        ApiException ex = new ApiException(CommonErrorCode.NOT_FOUND, "User not found");
        assertEquals(1002, ex.getCode());
        assertEquals("User not found", ex.getMessage());
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    void constructor_withData_shouldStoreData() {
        Map<String, String> data = Map.of("field", "email");
        ApiException ex = new ApiException(CommonErrorCode.VALIDATION_ERROR, data);
        assertEquals(data, ex.getData());
        assertTrue(ex.hasData());
    }

    @Test
    void constructor_withCause_shouldChainException() {
        RuntimeException cause = new RuntimeException("Original error");
        ApiException ex = new ApiException(CommonErrorCode.UNKNOWN_ERROR, cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void constructor_withCodeAndMessage_shouldSetDefaults() {
        ApiException ex = new ApiException(9999, "Custom error");
        assertEquals(9999, ex.getCode());
        assertEquals("Custom error", ex.getMessage());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    void constructor_withCodeMessageAndStatus_shouldSetAll() {
        ApiException ex = new ApiException(9999, "Custom error", 418);
        assertEquals(9999, ex.getCode());
        assertEquals("Custom error", ex.getMessage());
        assertEquals(418, ex.getHttpStatus());
    }

    @Test
    void builder_shouldCreateException() {
        ApiException ex = ApiException.builder()
            .errorCode(CommonErrorCode.FORBIDDEN)
            .message("Access denied")
            .data(Map.of("resource", "admin"))
            .build();

        assertEquals(1004, ex.getCode());
        assertEquals("Access denied", ex.getMessage());
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void builder_withCause_shouldChainException() {
        RuntimeException cause = new RuntimeException("Original");
        ApiException ex = ApiException.builder()
            .errorCode(CommonErrorCode.UNKNOWN_ERROR)
            .cause(cause)
            .build();

        assertEquals(cause, ex.getCause());
    }

    @Test
    void notFound_shouldCreateNotFoundException() {
        ApiException ex = ApiException.notFound("Item not found");
        assertEquals(1002, ex.getCode());
        assertEquals(404, ex.getHttpStatus());
        assertEquals("Item not found", ex.getMessage());
    }

    @Test
    void badRequest_shouldCreateBadRequestException() {
        ApiException ex = ApiException.badRequest("Invalid input");
        assertEquals(1001, ex.getCode());
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    void unauthorized_shouldCreateUnauthorizedException() {
        ApiException ex = ApiException.unauthorized("Token expired");
        assertEquals(1003, ex.getCode());
        assertEquals(401, ex.getHttpStatus());
    }

    @Test
    void forbidden_shouldCreateForbiddenException() {
        ApiException ex = ApiException.forbidden("No permission");
        assertEquals(1004, ex.getCode());
        assertEquals(403, ex.getHttpStatus());
    }

    @Test
    void conflict_shouldCreateConflictException() {
        ApiException ex = ApiException.conflict("Already exists");
        assertEquals(1005, ex.getCode());
        assertEquals(409, ex.getHttpStatus());
    }

    @Test
    void validationError_shouldCreateValidationException() {
        Map<String, String> errors = Map.of("email", "invalid format");
        ApiException ex = ApiException.validationError("Validation failed", errors);
        assertEquals(1006, ex.getCode());
        assertEquals(400, ex.getHttpStatus());
        assertEquals(errors, ex.getData());
    }

    @Test
    void serviceUnavailable_shouldCreateServiceException() {
        ApiException ex = ApiException.serviceUnavailable("Database down");
        assertEquals(3001, ex.getCode());
        assertEquals(503, ex.getHttpStatus());
    }

    @Test
    void internalError_shouldCreateInternalException() {
        ApiException ex = ApiException.internalError("Something went wrong");
        assertEquals(1000, ex.getCode());
        assertEquals(500, ex.getHttpStatus());
    }

    @Test
    void internalError_withCause_shouldChainException() {
        RuntimeException cause = new RuntimeException("DB error");
        ApiException ex = ApiException.internalError("Failed to save", cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void hasData_shouldReturnFalseWhenNoData() {
        ApiException ex = new ApiException(CommonErrorCode.NOT_FOUND);
        assertFalse(ex.hasData());
    }

    @Test
    void shouldExtendException() {
        ApiException ex = new ApiException(CommonErrorCode.NOT_FOUND);
        assertTrue(ex instanceof Exception);
        assertFalse(RuntimeException.class.isAssignableFrom(ex.getClass()));
    }

    @Test
    void toUnchecked_shouldConvertToRuntimeException() {
        ApiException checked = new ApiException(CommonErrorCode.NOT_FOUND, "User not found");
        ApiRuntimeException unchecked = checked.toUnchecked();

        assertEquals(checked.getCode(), unchecked.getCode());
        assertEquals(checked.getMessage(), unchecked.getMessage());
        assertEquals(checked.getHttpStatus(), unchecked.getHttpStatus());
        assertTrue(unchecked instanceof RuntimeException);
    }

    @Test
    void toUnchecked_shouldPreserveCause() {
        RuntimeException cause = new RuntimeException("Original");
        ApiException checked = new ApiException(CommonErrorCode.UNKNOWN_ERROR, cause);
        ApiRuntimeException unchecked = checked.toUnchecked();

        assertEquals(cause, unchecked.getCause());
    }

    @Test
    void builder_withCodeAndHttpStatus_shouldSetDirectly() {
        ApiException ex = ApiException.builder()
            .code(7777)
            .message("Custom error")
            .httpStatus(422)
            .build();
        assertEquals(7777, ex.getCode());
        assertEquals(422, ex.getHttpStatus());
        assertEquals("Custom error", ex.getMessage());
    }
}
