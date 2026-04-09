package dev.suprim.kit.exception;

/**
 * Interface for status codes. Implement this interface to create custom status code enums.
 */
public interface StatusCode {

    /**
     * Gets the status code.
     */
    int getCode();

    /**
     * Gets the status message.
     */
    String getMessage();

    /**
     * Gets the HTTP status code.
     * Default is 200 (OK).
     */
    default int getHttpStatus() {
        return 200;
    }
}
