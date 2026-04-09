package dev.suprim.kit.grpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcConstantsTest {

    @Test
    void constants_haveCorrectValues() {
        assertEquals("x-request-id", GrpcConstants.REQUEST_ID_KEY);
        assertEquals("x-trace-id", GrpcConstants.TRACE_ID_KEY);
        assertEquals("x-error-code", GrpcConstants.ERROR_CODE_KEY);
        assertEquals("x-error-data-bin", GrpcConstants.ERROR_DATA_KEY);
        assertEquals("x-user-id", GrpcConstants.USER_ID_KEY);
        assertEquals("x-tenant-id", GrpcConstants.TENANT_ID_KEY);
    }
}
