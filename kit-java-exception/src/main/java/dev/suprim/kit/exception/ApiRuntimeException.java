package dev.suprim.kit.exception;

import static java.util.Objects.nonNull;

/**
 * Unchecked exception for API errors with error code and optional data payload.
 * Extends RuntimeException - no throws declarations required.
 * For checked exceptions, use {@link ApiException} instead.
 */
public class ApiRuntimeException extends RuntimeException {

    private final int code;
    private final int httpStatus;
    private final Object data;

    /**
     * Creates an ApiRuntimeException from an StatusCode.
     */
    public ApiRuntimeException(StatusCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiRuntimeException from an StatusCode with custom message.
     */
    public ApiRuntimeException(StatusCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiRuntimeException from an StatusCode with data payload.
     */
    public ApiRuntimeException(StatusCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    /**
     * Creates an ApiRuntimeException from an StatusCode with custom message and data.
     */
    public ApiRuntimeException(StatusCode errorCode, String message, Object data) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    /**
     * Creates an ApiRuntimeException from an StatusCode with cause.
     */
    public ApiRuntimeException(StatusCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiRuntimeException from an StatusCode with custom message and cause.
     */
    public ApiRuntimeException(StatusCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiRuntimeException with explicit code and message.
     */
    public ApiRuntimeException(int code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = 500;
        this.data = null;
    }

    /**
     * Creates an ApiRuntimeException with explicit code, message, and HTTP status.
     */
    public ApiRuntimeException(int code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.data = null;
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Object getData() {
        return data;
    }

    public boolean hasData() {
        return nonNull(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int code = CommonErrorCode.UNKNOWN_ERROR.getCode();
        private String message = CommonErrorCode.UNKNOWN_ERROR.getMessage();
        private int httpStatus = 500;
        private Object data;
        private Throwable cause;

        public Builder errorCode(StatusCode errorCode) {
            this.code = errorCode.getCode();
            this.message = errorCode.getMessage();
            this.httpStatus = errorCode.getHttpStatus();
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder httpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public ApiRuntimeException build() {
            ApiRuntimeException ex = new ApiRuntimeException(code, message, httpStatus);
            if (nonNull(cause)) {
                ex.initCause(cause);
            }
            return ex;
        }
    }

    // Static factory methods

    public static ApiRuntimeException notFound(String message) {
        return new ApiRuntimeException(CommonErrorCode.NOT_FOUND, message);
    }

    public static ApiRuntimeException badRequest(String message) {
        return new ApiRuntimeException(CommonErrorCode.INVALID_REQUEST, message);
    }

    public static ApiRuntimeException unauthorized(String message) {
        return new ApiRuntimeException(CommonErrorCode.UNAUTHORIZED, message);
    }

    public static ApiRuntimeException forbidden(String message) {
        return new ApiRuntimeException(CommonErrorCode.FORBIDDEN, message);
    }

    public static ApiRuntimeException conflict(String message) {
        return new ApiRuntimeException(CommonErrorCode.CONFLICT, message);
    }

    public static ApiRuntimeException validationError(String message, Object errors) {
        return new ApiRuntimeException(CommonErrorCode.VALIDATION_ERROR, message, errors);
    }

    public static ApiRuntimeException serviceUnavailable(String message) {
        return new ApiRuntimeException(CommonErrorCode.SERVICE_UNAVAILABLE, message);
    }

    public static ApiRuntimeException internalError(String message) {
        return new ApiRuntimeException(CommonErrorCode.UNKNOWN_ERROR, message);
    }

    public static ApiRuntimeException internalError(String message, Throwable cause) {
        return new ApiRuntimeException(CommonErrorCode.UNKNOWN_ERROR, message, cause);
    }
}
