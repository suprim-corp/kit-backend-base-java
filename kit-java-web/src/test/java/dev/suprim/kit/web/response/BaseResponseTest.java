package dev.suprim.kit.web.response;

import dev.suprim.kit.exception.ApiStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTest {

    @Test
    void defaultConstructor_setsSuccessCode() {
        BaseResponse<Void> response = new BaseResponse<>();

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(ApiStatus.SUCCESS.getMessage(), response.message());
        assertNull(response.data());
    }

    @Test
    void dataConstructor_setsSuccessWithData() {
        String data = "test data";
        BaseResponse<String> response = new BaseResponse<>(data);

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(ApiStatus.SUCCESS.getMessage(), response.message());
        assertEquals("test data", response.data());
    }

    @Test
    void statusConstructor_setsStatusWithNoData() {
        BaseResponse<Void> response = new BaseResponse<>(ApiStatus.NOT_FOUND);

        assertEquals(ApiStatus.NOT_FOUND.getCode(), response.code());
        assertEquals(ApiStatus.NOT_FOUND.getMessage(), response.message());
        assertNull(response.data());
    }

    @Test
    void statusMessageConstructor_setsStatusWithCustomMessage() {
        BaseResponse<Void> response = new BaseResponse<>(ApiStatus.NOT_FOUND, "User not found");

        assertEquals(ApiStatus.NOT_FOUND.getCode(), response.code());
        assertEquals("User not found", response.message());
        assertNull(response.data());
    }

    @Test
    void statusDataConstructor_setsStatusWithData() {
        // When T=String, (ApiStatus, String) matches (ApiStatus, String message) constructor
        // So this test verifies that behavior - the second param becomes message
        String data = "error details";
        BaseResponse<String> response = new BaseResponse<>(ApiStatus.INVALID_REQUEST, data);

        assertEquals(ApiStatus.INVALID_REQUEST.getCode(), response.code());
        assertEquals("error details", response.message());
        assertNull(response.data());
    }

    @Test
    void statusDataConstructor_setsStatusWithNonStringData() {
        Integer data = 42;
        BaseResponse<Integer> response = new BaseResponse<>(ApiStatus.INVALID_REQUEST, data);

        assertEquals(ApiStatus.INVALID_REQUEST.getCode(), response.code());
        assertEquals(ApiStatus.INVALID_REQUEST.getMessage(), response.message());
        assertEquals(42, response.data());
    }

    @Test
    void fullConstructor_setsAllFields() {
        String data = "full data";
        BaseResponse<String> response = new BaseResponse<>(ApiStatus.SUCCESS, "Custom message", data);

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals("Custom message", response.message());
        assertEquals("full data", response.data());
    }

    @Test
    void success_returnsSuccessWithNullData() {
        BaseResponse<Void> response = BaseResponse.success();

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(ApiStatus.SUCCESS.getMessage(), response.message());
        assertNull(response.data());
    }

    @Test
    void successWithData_returnsSuccessWithData() {
        BaseResponse<String> response = BaseResponse.success("hello");

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(ApiStatus.SUCCESS.getMessage(), response.message());
        assertEquals("hello", response.data());
    }

    @Test
    void successWithNullData_noAmbiguity() {
        BaseResponse<String> response = BaseResponse.success(null);

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(ApiStatus.SUCCESS.getMessage(), response.message());
        assertNull(response.data());
    }

    @Test
    void canonicalConstructor_setsAllFieldsDirectly() {
        BaseResponse<Integer> response = new BaseResponse<>(200, "OK", 42);

        assertEquals(200, response.code());
        assertEquals("OK", response.message());
        assertEquals(42, response.data());
    }

}
