package dev.suprim.kit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiStatusTest {

    @Test
    void shouldImplementStatusCode() {
        assertTrue(ApiStatus.NOT_FOUND instanceof StatusCode);
    }

    @Test
    void allStatusesShouldReturnHttp200() {
        for (ApiStatus status : ApiStatus.values()) {
            assertEquals(200, status.getHttpStatus());
        }
    }

    @Test
    void shouldHaveCorrectCodes() {
        assertEquals(1, ApiStatus.SUCCESS.getCode());
        assertEquals(99, ApiStatus.SERVER_ERROR.getCode());
        assertEquals(400, ApiStatus.INVALID_REQUEST.getCode());
        assertEquals(404, ApiStatus.NOT_FOUND.getCode());
        assertEquals(401, ApiStatus.UNAUTHORIZED.getCode());
        assertEquals(403, ApiStatus.FORBIDDEN.getCode());
        assertEquals(409, ApiStatus.CONFLICT.getCode());
        assertEquals(503, ApiStatus.NOT_CONFIGURED.getCode());
    }

    @Test
    void shouldHaveMessages() {
        assertEquals("Success!", ApiStatus.SUCCESS.getMessage());
        assertEquals("Not found!", ApiStatus.NOT_FOUND.getMessage());
    }

    @Test
    void get_shouldReturnStatusByCode() {
        assertEquals(ApiStatus.NOT_FOUND, ApiStatus.get(404).orElse(null));
        assertEquals(ApiStatus.SUCCESS, ApiStatus.get(1).orElse(null));
    }

    @Test
    void get_shouldReturnEmptyForUnknownCode() {
        assertTrue(ApiStatus.get(9999).isEmpty());
        assertTrue(ApiStatus.get(null).isEmpty());
    }

    @Test
    void shouldWorkWithApiException() {
        ApiException ex = new ApiException(ApiStatus.NOT_FOUND);
        assertEquals(404, ex.getCode());
        assertEquals("Not found!", ex.getMessage());
        assertEquals(200, ex.getHttpStatus());
    }

    @Test
    void shouldWorkWithApiRuntimeException() {
        ApiRuntimeException ex = new ApiRuntimeException(ApiStatus.UNAUTHORIZED);
        assertEquals(401, ex.getCode());
        assertEquals("Unauthorized!", ex.getMessage());
        assertEquals(200, ex.getHttpStatus());
    }

    @Test
    void getMap_shouldReturnAllStatuses() {
        var map = ApiStatus.getMap();
        assertNotNull(map);
        assertEquals(ApiStatus.values().length, map.size());
        assertEquals(ApiStatus.SUCCESS, map.get(1));
        assertEquals(ApiStatus.NOT_FOUND, map.get(404));
    }
}