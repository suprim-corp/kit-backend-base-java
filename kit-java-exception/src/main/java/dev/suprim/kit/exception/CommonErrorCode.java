package dev.suprim.kit.exception;

/**
 * Common error codes for general use.
 */
public enum CommonErrorCode implements StatusCode {

    // General errors (1xxx)
    UNKNOWN_ERROR(1000, "Unknown error occurred", 500),
    INVALID_REQUEST(1001, "Invalid request", 400),
    NOT_FOUND(1002, "Resource not found", 404),
    UNAUTHORIZED(1003, "Unauthorized access", 401),
    FORBIDDEN(1004, "Access forbidden", 403),
    CONFLICT(1005, "Resource conflict", 409),
    VALIDATION_ERROR(1006, "Validation error", 400),

    // Data errors (2xxx)
    DATA_NOT_FOUND(2001, "Data not found", 404),
    DATA_ALREADY_EXISTS(2002, "Data already exists", 409),
    DATA_INTEGRITY_ERROR(2003, "Data integrity error", 400),
    INVALID_DATA(2004, "Invalid data format", 400),

    // Service errors (3xxx)
    SERVICE_UNAVAILABLE(3001, "Service unavailable", 503),
    TIMEOUT(3002, "Request timeout", 504),
    RATE_LIMITED(3003, "Rate limit exceeded", 429),
    EXTERNAL_SERVICE_ERROR(3004, "External service error", 502);

    private final int code;
    private final String message;
    private final int httpStatus;

    CommonErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}
