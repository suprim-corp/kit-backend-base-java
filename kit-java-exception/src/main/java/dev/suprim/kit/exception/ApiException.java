package dev.suprim.kit.exception;

import static java.util.Objects.nonNull;

/**
 * Checked exception for API errors with error code and optional data payload.
 * Requires explicit handling via throws declarations.
 * For unchecked exceptions, use {@link ApiRuntimeException} instead.
 */
public class ApiException extends Exception {

    private final int code;
    private final int httpStatus;
    private final Object data;

    /**
     * Creates an ApiException from an StatusCode.
     */
    public ApiException(StatusCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiException from an StatusCode with custom message.
     */
    public ApiException(StatusCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiException from an StatusCode with data payload.
     */
    public ApiException(StatusCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    /**
     * Creates an ApiException from an StatusCode with custom message and data.
     */
    public ApiException(StatusCode errorCode, String message, Object data) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = data;
    }

    /**
     * Creates an ApiException from an StatusCode with cause.
     */
    public ApiException(StatusCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiException from an StatusCode with custom message and cause.
     */
    public ApiException(StatusCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
        this.data = null;
    }

    /**
     * Creates an ApiException with explicit code and message.
     */
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = 500;
        this.data = null;
    }

    /**
     * Creates an ApiException with explicit code, message, and HTTP status.
     */
    public ApiException(int code, String message, int httpStatus) {
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

        public ApiException build() {
            ApiException ex = new ApiException(code, message, httpStatus);
            if (nonNull(cause)) {
                ex.initCause(cause);
            }
            return ex;
        }
    }

    // Static factory methods

    public static ApiException notFound(String message) {
        return new ApiException(CommonErrorCode.NOT_FOUND, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(CommonErrorCode.INVALID_REQUEST, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(CommonErrorCode.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(CommonErrorCode.FORBIDDEN, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(CommonErrorCode.CONFLICT, message);
    }

    public static ApiException validationError(String message, Object errors) {
        return new ApiException(CommonErrorCode.VALIDATION_ERROR, message, errors);
    }

    public static ApiException serviceUnavailable(String message) {
        return new ApiException(CommonErrorCode.SERVICE_UNAVAILABLE, message);
    }

    public static ApiException internalError(String message) {
        return new ApiException(CommonErrorCode.UNKNOWN_ERROR, message);
    }

    public static ApiException internalError(String message, Throwable cause) {
        return new ApiException(CommonErrorCode.UNKNOWN_ERROR, message, cause);
    }

    /**
     * Converts this checked exception to an unchecked ApiRuntimeException.
     */
    public ApiRuntimeException toUnchecked() {
        ApiRuntimeException unchecked = new ApiRuntimeException(code, getMessage(), httpStatus);
        if (nonNull(getCause())) {
            unchecked.initCause(getCause());
        }
        return unchecked;
    }
}
