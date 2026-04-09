package dev.suprim.kit.grpc;

import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrpcStatusMapperTest {

    @ParameterizedTest
    @CsvSource({
            "200, OK",
            "201, OK",
            "204, OK",
            "400, INVALID_ARGUMENT",
            "401, UNAUTHENTICATED",
            "403, PERMISSION_DENIED",
            "404, NOT_FOUND",
            "409, ALREADY_EXISTS",
            "429, RESOURCE_EXHAUSTED",
            "499, CANCELLED",
            "500, INTERNAL",
            "501, UNIMPLEMENTED",
            "502, UNAVAILABLE",
            "503, UNAVAILABLE",
            "504, DEADLINE_EXCEEDED"
    })
    void fromHttpStatus_mapsCorrectly(int httpStatus, String expectedCode) {
        Status result = GrpcStatusMapper.fromHttpStatus(httpStatus);
        assertEquals(Status.Code.valueOf(expectedCode), result.getCode());
    }

    @Test
    void fromHttpStatus_unknownClientError_returnsPreconditionFailed() {
        Status result = GrpcStatusMapper.fromHttpStatus(418);
        assertEquals(Status.Code.FAILED_PRECONDITION, result.getCode());
    }

    @Test
    void fromHttpStatus_unknownServerError_returnsInternal() {
        Status result = GrpcStatusMapper.fromHttpStatus(599);
        assertEquals(Status.Code.INTERNAL, result.getCode());
    }

    @Test
    void fromHttpStatus_unknownNon4xxNon5xx_returnsInternal() {
        // 302 is not in explicit cases and < 400, so hits default INTERNAL branch
        Status result = GrpcStatusMapper.fromHttpStatus(302);
        assertEquals(Status.Code.INTERNAL, result.getCode());
    }

    @ParameterizedTest
    @CsvSource({
            "OK, 200",
            "CANCELLED, 499",
            "UNKNOWN, 500",
            "INVALID_ARGUMENT, 400",
            "DEADLINE_EXCEEDED, 504",
            "NOT_FOUND, 404",
            "ALREADY_EXISTS, 409",
            "PERMISSION_DENIED, 403",
            "RESOURCE_EXHAUSTED, 429",
            "FAILED_PRECONDITION, 400",
            "ABORTED, 409",
            "OUT_OF_RANGE, 400",
            "UNIMPLEMENTED, 501",
            "INTERNAL, 500",
            "UNAVAILABLE, 503",
            "DATA_LOSS, 500",
            "UNAUTHENTICATED, 401"
    })
    void toHttpStatus_mapsCorrectly(String statusCode, int expectedHttp) {
        Status status = Status.fromCode(Status.Code.valueOf(statusCode));
        int result = GrpcStatusMapper.toHttpStatus(status);
        assertEquals(expectedHttp, result);
    }
}
