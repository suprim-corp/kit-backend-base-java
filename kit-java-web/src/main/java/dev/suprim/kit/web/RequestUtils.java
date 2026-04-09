package dev.suprim.kit.web;

import jakarta.servlet.http.HttpServletRequest;
import dev.suprim.kit.core.DataUtils;

/**
 * Utilities for working with HTTP requests.
 */
public final class RequestUtils {

    private RequestUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets the client IP address from the request, considering proxy headers.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Check X-Forwarded-For header first (may contain multiple IPs)
        String xForwardedFor = request.getHeader(HttpConstants.HEADER_X_FORWARDED_FOR);
        if (!DataUtils.isNullOrBlank(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, first one is the client
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeader(HttpConstants.HEADER_X_REAL_IP);
        if (!DataUtils.isNullOrBlank(xRealIp)) {
            return xRealIp.trim();
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    /**
     * Extracts the Bearer token from the Authorization header.
     * Returns null if no Bearer token is present.
     */
    public static String extractBearerToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authHeader = request.getHeader(HttpConstants.HEADER_AUTHORIZATION);
        return extractBearerToken(authHeader);
    }

    /**
     * Extracts the Bearer token from an Authorization header value.
     * Returns null if no Bearer token is present.
     */
    public static String extractBearerToken(String authHeader) {
        if (DataUtils.isNullOrBlank(authHeader)) {
            return null;
        }

        if (authHeader.toLowerCase().startsWith("bearer ")) {
            return authHeader.substring(7).trim();
        }

        return null;
    }

    /**
     * Extracts the Basic auth credentials from the Authorization header.
     * Returns null if no Basic auth is present.
     */
    public static String extractBasicAuth(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authHeader = request.getHeader(HttpConstants.HEADER_AUTHORIZATION);
        if (DataUtils.isNullOrBlank(authHeader)) {
            return null;
        }

        if (authHeader.toLowerCase().startsWith("basic ")) {
            return authHeader.substring(6).trim();
        }

        return null;
    }

    /**
     * Gets the request URI without the context path.
     */
    public static String getRequestPath(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }

        return uri;
    }

    /**
     * Gets the full request URL including query string.
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString != null) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    /**
     * Checks if the request is an AJAX/XHR request.
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
    }

    /**
     * Gets the User-Agent header from the request.
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader(HttpConstants.HEADER_USER_AGENT);
    }

    /**
     * Gets a header value or returns a default if not present.
     */
    public static String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        if (request == null) {
            return defaultValue;
        }
        String value = request.getHeader(headerName);
        return DataUtils.isNullOrBlank(value) ? defaultValue : value;
    }
}
