package dev.suprim.kit.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Standard API status codes for all backends.
 * All responses return HTTP 200, with the code in the response body.
 */
public enum ApiStatus implements StatusCode {
    SUCCESS(1, "Success!"),
    SERVER_ERROR(99, "The server is being upgraded, please try again later!"),
    INVALID_REQUEST(400, "Invalid request!"),
    NOT_FOUND(404, "Not found!"),
    UNAUTHORIZED(401, "Unauthorized!"),
    FORBIDDEN(403, "Forbidden!"),
    CONFLICT(409, "Conflict!"),
    NOT_CONFIGURED(503, "Service not configured!");

    private final int code;
    private final String message;

    private static final Map<Integer, ApiStatus> MAP = new HashMap<>();

    static {
        for (ApiStatus status : values()) {
            MAP.put(status.getCode(), status);
        }
    }

    ApiStatus(int code, String message) {
        this.code = code;
        this.message = message;
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
        return 200;
    }

    public static Optional<ApiStatus> get(Integer code) {
        if (isNull(code)) {
            return Optional.empty();
        }
        return Optional.ofNullable(MAP.get(code));
    }

    public static Map<Integer, ApiStatus> getMap() {
        return MAP;
    }
}
