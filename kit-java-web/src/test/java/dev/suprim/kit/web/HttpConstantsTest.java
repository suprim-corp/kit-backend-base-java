package dev.suprim.kit.web;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class HttpConstantsTest {

    @Test
    void headers_haveCorrectValues() {
        assertEquals("Content-Type", HttpConstants.HEADER_CONTENT_TYPE);
        assertEquals("Authorization", HttpConstants.HEADER_AUTHORIZATION);
        assertEquals("Accept", HttpConstants.HEADER_ACCEPT);
        assertEquals("User-Agent", HttpConstants.HEADER_USER_AGENT);
        assertEquals("Origin", HttpConstants.HEADER_ORIGIN);
        assertEquals("Referer", HttpConstants.HEADER_REFERER);
    }

    @Test
    void proxyHeaders_haveCorrectValues() {
        assertEquals("X-Forwarded-For", HttpConstants.HEADER_X_FORWARDED_FOR);
        assertEquals("X-Forwarded-Host", HttpConstants.HEADER_X_FORWARDED_HOST);
        assertEquals("X-Forwarded-Proto", HttpConstants.HEADER_X_FORWARDED_PROTO);
        assertEquals("X-Real-IP", HttpConstants.HEADER_X_REAL_IP);
    }

    @Test
    void customHeaders_haveCorrectValues() {
        assertEquals("X-Request-ID", HttpConstants.HEADER_X_REQUEST_ID);
        assertEquals("X-Correlation-ID", HttpConstants.HEADER_X_CORRELATION_ID);
        assertEquals("X-UserInfo", HttpConstants.HEADER_X_USER_INFO);
        assertEquals("X-Trace-ID", HttpConstants.HEADER_X_TRACE_ID);
    }

    @Test
    void contentTypes_haveCorrectValues() {
        assertEquals("application/json", HttpConstants.CONTENT_TYPE_JSON);
        assertEquals("application/json; charset=utf-8", HttpConstants.CONTENT_TYPE_JSON_UTF8);
        assertEquals("application/xml", HttpConstants.CONTENT_TYPE_XML);
        assertEquals("application/x-www-form-urlencoded", HttpConstants.CONTENT_TYPE_FORM);
        assertEquals("multipart/form-data", HttpConstants.CONTENT_TYPE_MULTIPART);
        assertEquals("text/plain", HttpConstants.CONTENT_TYPE_TEXT);
        assertEquals("text/html", HttpConstants.CONTENT_TYPE_HTML);
    }

    @Test
    void httpMethods_haveCorrectValues() {
        assertEquals("GET", HttpConstants.METHOD_GET);
        assertEquals("POST", HttpConstants.METHOD_POST);
        assertEquals("PUT", HttpConstants.METHOD_PUT);
        assertEquals("DELETE", HttpConstants.METHOD_DELETE);
        assertEquals("PATCH", HttpConstants.METHOD_PATCH);
        assertEquals("OPTIONS", HttpConstants.METHOD_OPTIONS);
        assertEquals("HEAD", HttpConstants.METHOD_HEAD);
    }

    @Test
    void tokenPrefixes_haveCorrectValues() {
        assertEquals("Bearer ", HttpConstants.TOKEN_PREFIX_BEARER);
        assertEquals("Basic ", HttpConstants.TOKEN_PREFIX_BASIC);
    }

    @Test
    void cacheControl_haveCorrectValues() {
        assertEquals("no-cache", HttpConstants.CACHE_NO_CACHE);
        assertEquals("no-store", HttpConstants.CACHE_NO_STORE);
        assertEquals("private", HttpConstants.CACHE_PRIVATE);
        assertEquals("public", HttpConstants.CACHE_PUBLIC);
    }

    @Test
    void constructor_throwsUnsupportedOperationException() throws Exception {
        Constructor<HttpConstants> constructor = HttpConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    }
}
