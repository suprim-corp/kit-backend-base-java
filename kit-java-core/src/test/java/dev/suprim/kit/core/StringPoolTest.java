package dev.suprim.kit.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringPoolTest {

    @Test
    void httpHeaders_haveCorrectValues() {
        assertEquals("Content-Type", StringPool.HEADER_CONTENT_TYPE);
        assertEquals("Authorization", StringPool.HEADER_AUTHORIZATION);
        assertEquals("Accept", StringPool.HEADER_ACCEPT);
        assertEquals("X-Forwarded-For", StringPool.HEADER_X_FORWARDED_FOR);
        assertEquals("X-Real-IP", StringPool.HEADER_X_REAL_IP);
        assertEquals("x-userinfo", StringPool.HEADER_X_USER_INFO);
    }

    @Test
    void httpMethods_haveCorrectValues() {
        assertEquals("GET", StringPool.METHOD_GET);
        assertEquals("POST", StringPool.METHOD_POST);
        assertEquals("PUT", StringPool.METHOD_PUT);
        assertEquals("DELETE", StringPool.METHOD_DELETE);
        assertEquals("PATCH", StringPool.METHOD_PATCH);
    }

    @Test
    void contentTypes_haveCorrectValues() {
        assertEquals("application/json", StringPool.CONTENT_TYPE_JSON);
        assertEquals("application/x-www-form-urlencoded", StringPool.CONTENT_TYPE_FORM);
        assertEquals("multipart/form-data", StringPool.CONTENT_TYPE_MULTIPART);
    }

    @Test
    void tokenPrefixes_haveCorrectValues() {
        assertEquals("Bearer ", StringPool.TOKEN_PREFIX_BEARER);
        assertEquals("Basic ", StringPool.TOKEN_PREFIX_BASIC);
    }

    @Test
    void commonValues_haveCorrectValues() {
        assertEquals("unknown", StringPool.UNKNOWN);
        assertEquals("", StringPool.EMPTY);
        assertEquals(",", StringPool.COMMA);
        assertEquals(";", StringPool.SEMICOLON);
        assertEquals(":", StringPool.COLON);
        assertEquals(" ", StringPool.SPACE);
        assertEquals("=", StringPool.EQUALS);
        assertEquals("&", StringPool.AMPERSAND);
        assertEquals("?", StringPool.QUESTION_MARK);
        assertEquals("/", StringPool.SLASH);
        assertEquals(".", StringPool.DOT);
        assertEquals("_", StringPool.UNDERSCORE);
        assertEquals("-", StringPool.HYPHEN);
    }

    @Test
    void booleanStrings_haveCorrectValues() {
        assertEquals("true", StringPool.TRUE);
        assertEquals("false", StringPool.FALSE);
    }

    @Test
    void maskedValue_hasCorrectValue() {
        assertEquals("********", StringPool.MASKED_VALUE);
    }
}
