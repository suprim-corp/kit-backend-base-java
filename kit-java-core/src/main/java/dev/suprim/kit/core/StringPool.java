package dev.suprim.kit.core;

/**
 * Common string constants for HTTP headers and web operations.
 */
public interface StringPool {

    // HTTP Headers
    String HEADER_CONTENT_TYPE = "Content-Type";
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_ACCEPT = "Accept";
    String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    String HEADER_X_REAL_IP = "X-Real-IP";
    String HEADER_X_USER_INFO = "x-userinfo";

    // HTTP Methods
    String METHOD_GET = "GET";
    String METHOD_POST = "POST";
    String METHOD_PUT = "PUT";
    String METHOD_DELETE = "DELETE";
    String METHOD_PATCH = "PATCH";

    // Content Types
    String CONTENT_TYPE_JSON = "application/json";
    String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // Token
    String TOKEN_PREFIX_BEARER = "Bearer ";
    String TOKEN_PREFIX_BASIC = "Basic ";

    // Common Values
    String UNKNOWN = "unknown";
    String EMPTY = "";
    String COMMA = ",";
    String SEMICOLON = ";";
    String COLON = ":";
    String SPACE = " ";
    String EQUALS = "=";
    String AMPERSAND = "&";
    String QUESTION_MARK = "?";
    String SLASH = "/";
    String DOT = ".";
    String UNDERSCORE = "_";
    String HYPHEN = "-";

    // Boolean strings
    String TRUE = "true";
    String FALSE = "false";

    // Masked value for sensitive data
    String MASKED_VALUE = "********";
}
