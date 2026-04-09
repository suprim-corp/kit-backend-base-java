package dev.suprim.kit.grpc;

import io.grpc.Status;

/**
 * Maps HTTP status codes to gRPC Status codes.
 */
public final class GrpcStatusMapper {

    private GrpcStatusMapper() {
    }

    /**
     * Maps an HTTP status code to gRPC Status.
     *
     * @param httpStatus the HTTP status code
     * @return the corresponding gRPC Status
     */
    public static Status fromHttpStatus(int httpStatus) {
        return switch (httpStatus) {
            case 200, 201, 204 -> Status.OK;
            case 400 -> Status.INVALID_ARGUMENT;
            case 401 -> Status.UNAUTHENTICATED;
            case 403 -> Status.PERMISSION_DENIED;
            case 404 -> Status.NOT_FOUND;
            case 409 -> Status.ALREADY_EXISTS;
            case 429 -> Status.RESOURCE_EXHAUSTED;
            case 499 -> Status.CANCELLED;
            case 500 -> Status.INTERNAL;
            case 501 -> Status.UNIMPLEMENTED;
            case 502 -> Status.UNAVAILABLE;
            case 503 -> Status.UNAVAILABLE;
            case 504 -> Status.DEADLINE_EXCEEDED;
            default -> {
                if (httpStatus >= 400 && httpStatus < 500) {
                    yield Status.FAILED_PRECONDITION;
                }
                yield Status.INTERNAL;
            }
        };
    }

    /**
     * Maps a gRPC Status code to HTTP status code.
     *
     * @param status the gRPC Status
     * @return the corresponding HTTP status code
     */
    public static int toHttpStatus(Status status) {
        return switch (status.getCode()) {
            case OK -> 200;
            case CANCELLED -> 499;
            case UNKNOWN -> 500;
            case INVALID_ARGUMENT -> 400;
            case DEADLINE_EXCEEDED -> 504;
            case NOT_FOUND -> 404;
            case ALREADY_EXISTS -> 409;
            case PERMISSION_DENIED -> 403;
            case RESOURCE_EXHAUSTED -> 429;
            case FAILED_PRECONDITION -> 400;
            case ABORTED -> 409;
            case OUT_OF_RANGE -> 400;
            case UNIMPLEMENTED -> 501;
            case INTERNAL -> 500;
            case UNAVAILABLE -> 503;
            case DATA_LOSS -> 500;
            case UNAUTHENTICATED -> 401;
        };
    }
}
