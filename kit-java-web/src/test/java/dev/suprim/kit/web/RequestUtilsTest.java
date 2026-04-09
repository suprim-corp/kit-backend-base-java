package dev.suprim.kit.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestUtilsTest {

    @Test
    void extractBearerToken_shouldExtractToken() {
        assertEquals("abc123", RequestUtils.extractBearerToken("Bearer abc123"));
    }

    @Test
    void extractBearerToken_shouldHandleCaseInsensitive() {
        assertEquals("ABC123", RequestUtils.extractBearerToken("bearer ABC123"));
    }

    @Test
    void extractBearerToken_shouldReturnNullForNull() {
        assertNull(RequestUtils.extractBearerToken((String) null));
    }

    @Test
    void extractBearerToken_shouldReturnNullForEmpty() {
        assertNull(RequestUtils.extractBearerToken(""));
    }

    @Test
    void extractBearerToken_shouldReturnNullForBlank() {
        assertNull(RequestUtils.extractBearerToken("   "));
    }

    @Test
    void extractBearerToken_shouldReturnNullForNonBearer() {
        assertNull(RequestUtils.extractBearerToken("Basic abc123"));
    }

    @Test
    void extractBearerToken_shouldTrimToken() {
        assertEquals("abc123", RequestUtils.extractBearerToken("Bearer   abc123  "));
    }

    @Test
    void extractBearerToken_fromRequest_shouldExtractToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_AUTHORIZATION)).thenReturn("Bearer token123");

        assertEquals("token123", RequestUtils.extractBearerToken(request));
    }

    @Test
    void extractBearerToken_fromRequest_shouldReturnNullForNullRequest() {
        assertNull(RequestUtils.extractBearerToken((HttpServletRequest) null));
    }

    @Test
    void getClientIpAddress_shouldReturnNullForNullRequest() {
        assertNull(RequestUtils.getClientIpAddress(null));
    }

    @Test
    void getClientIpAddress_shouldReturnXForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_X_FORWARDED_FOR)).thenReturn("192.168.1.1, 10.0.0.1");

        assertEquals("192.168.1.1", RequestUtils.getClientIpAddress(request));
    }

    @Test
    void getClientIpAddress_shouldReturnXRealIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_X_FORWARDED_FOR)).thenReturn(null);
        when(request.getHeader(HttpConstants.HEADER_X_REAL_IP)).thenReturn("  10.0.0.5  ");

        assertEquals("10.0.0.5", RequestUtils.getClientIpAddress(request));
    }

    @Test
    void getClientIpAddress_shouldFallbackToRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_X_FORWARDED_FOR)).thenReturn(null);
        when(request.getHeader(HttpConstants.HEADER_X_REAL_IP)).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        assertEquals("127.0.0.1", RequestUtils.getClientIpAddress(request));
    }

    @Test
    void extractBasicAuth_shouldExtractCredentials() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        assertEquals("dXNlcjpwYXNz", RequestUtils.extractBasicAuth(request));
    }

    @Test
    void extractBasicAuth_shouldReturnNullForNullRequest() {
        assertNull(RequestUtils.extractBasicAuth(null));
    }

    @Test
    void extractBasicAuth_shouldReturnNullForNullHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_AUTHORIZATION)).thenReturn(null);

        assertNull(RequestUtils.extractBasicAuth(request));
    }

    @Test
    void extractBasicAuth_shouldReturnNullForNonBasic() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_AUTHORIZATION)).thenReturn("Bearer token");

        assertNull(RequestUtils.extractBasicAuth(request));
    }

    @Test
    void getRequestPath_shouldReturnNullForNullRequest() {
        assertNull(RequestUtils.getRequestPath(null));
    }

    @Test
    void getRequestPath_shouldRemoveContextPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/app/api/users");
        when(request.getContextPath()).thenReturn("/app");

        assertEquals("/api/users", RequestUtils.getRequestPath(request));
    }

    @Test
    void getRequestPath_shouldReturnUriWhenNoContextPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getContextPath()).thenReturn("");

        assertEquals("/api/users", RequestUtils.getRequestPath(request));
    }

    @Test
    void getRequestPath_shouldReturnUriWhenContextPathNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getContextPath()).thenReturn(null);

        assertEquals("/api/users", RequestUtils.getRequestPath(request));
    }

    @Test
    void getRequestPath_shouldReturnUriWhenUriDoesNotStartWithContextPath() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getContextPath()).thenReturn("/other");

        assertEquals("/api/users", RequestUtils.getRequestPath(request));
    }

    @Test
    void getFullRequestUrl_shouldReturnNullForNullRequest() {
        assertNull(RequestUtils.getFullRequestUrl(null));
    }

    @Test
    void getFullRequestUrl_shouldIncludeQueryString() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com/api"));
        when(request.getQueryString()).thenReturn("page=1&size=10");

        assertEquals("http://example.com/api?page=1&size=10", RequestUtils.getFullRequestUrl(request));
    }

    @Test
    void getFullRequestUrl_shouldWorkWithoutQueryString() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com/api"));
        when(request.getQueryString()).thenReturn(null);

        assertEquals("http://example.com/api", RequestUtils.getFullRequestUrl(request));
    }

    @Test
    void isAjaxRequest_shouldReturnFalseForNullRequest() {
        assertFalse(RequestUtils.isAjaxRequest(null));
    }

    @Test
    void isAjaxRequest_shouldReturnTrueForXMLHttpRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        assertTrue(RequestUtils.isAjaxRequest(request));
    }

    @Test
    void isAjaxRequest_shouldReturnTrueForCaseInsensitive() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Requested-With")).thenReturn("xmlhttprequest");

        assertTrue(RequestUtils.isAjaxRequest(request));
    }

    @Test
    void isAjaxRequest_shouldReturnFalseForOtherValues() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Requested-With")).thenReturn("SomeOtherValue");

        assertFalse(RequestUtils.isAjaxRequest(request));
    }

    @Test
    void getUserAgent_shouldReturnNullForNullRequest() {
        assertNull(RequestUtils.getUserAgent(null));
    }

    @Test
    void getUserAgent_shouldReturnUserAgent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpConstants.HEADER_USER_AGENT)).thenReturn("Mozilla/5.0");

        assertEquals("Mozilla/5.0", RequestUtils.getUserAgent(request));
    }

    @Test
    void getHeaderOrDefault_shouldReturnDefaultForNullRequest() {
        assertEquals("default", RequestUtils.getHeaderOrDefault(null, "Header", "default"));
    }

    @Test
    void getHeaderOrDefault_shouldReturnHeaderValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Custom-Header")).thenReturn("custom-value");

        assertEquals("custom-value", RequestUtils.getHeaderOrDefault(request, "Custom-Header", "default"));
    }

    @Test
    void getHeaderOrDefault_shouldReturnDefaultForBlankHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Custom-Header")).thenReturn("   ");

        assertEquals("default", RequestUtils.getHeaderOrDefault(request, "Custom-Header", "default"));
    }

    @Test
    void getHeaderOrDefault_shouldReturnDefaultForNullHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Custom-Header")).thenReturn(null);

        assertEquals("default", RequestUtils.getHeaderOrDefault(request, "Custom-Header", "default"));
    }

    @Test
    void constructor_throwsUnsupportedOperationException() throws Exception {
        Constructor<RequestUtils> constructor = RequestUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    }
}
