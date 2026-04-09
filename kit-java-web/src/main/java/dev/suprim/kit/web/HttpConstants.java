package dev.suprim.kit.web;

/**
 * HTTP-related constants for web applications.
 */
public final class HttpConstants {

    private HttpConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // HTTP Headers
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_ORIGIN = "Origin";
    public static final String HEADER_REFERER = "Referer";

    // Proxy Headers
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";
    public static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String HEADER_X_REAL_IP = "X-Real-IP";

    // Custom Headers
    public static final String HEADER_X_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_X_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_X_USER_INFO = "X-UserInfo";
    public static final String HEADER_X_TRACE_ID = "X-Trace-ID";

    // CORS Headers
    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String HEADER_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    // Content Types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

    // HTTP Methods
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_HEAD = "HEAD";

    // Token Prefixes
    public static final String TOKEN_PREFIX_BEARER = "Bearer ";
    public static final String TOKEN_PREFIX_BASIC = "Basic ";

    // Cache Control Values
    public static final String CACHE_NO_CACHE = "no-cache";
    public static final String CACHE_NO_STORE = "no-store";
    public static final String CACHE_PRIVATE = "private";
    public static final String CACHE_PUBLIC = "public";
}
