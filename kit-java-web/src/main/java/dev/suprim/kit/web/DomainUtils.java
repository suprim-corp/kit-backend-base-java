package dev.suprim.kit.web;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.Objects;

/**
 * Utility class for extracting domain information from HTTP requests.
 */
public final class DomainUtils {

    private DomainUtils() {
        // Utility class
    }

    /**
     * Extract domain from request headers.
     * Priority: X-Forwarded-Host > Origin > Referer > Host
     * Note: Port is preserved to allow matching domains like "localhost:3000"
     *
     * @param request the HTTP request
     * @return the extracted domain (with port if present) or null if not found
     */
    public static String extractDomain(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }

        // Try X-Forwarded-Host first (set by reverse proxies)
        String forwardedHost = request.getHeader(HttpConstants.HEADER_X_FORWARDED_HOST);
        if (Objects.nonNull(forwardedHost) && !forwardedHost.isBlank()) {
            // May contain multiple hosts, take the first one (preserve port)
            return forwardedHost.split(",")[0].trim();
        }

        // Try Origin header (set by browsers for CORS requests)
        String origin = request.getHeader(HttpConstants.HEADER_ORIGIN);
        if (Objects.nonNull(origin) && !origin.isBlank()) {
            String domain = parseHostFromUrl(origin);
            if (Objects.nonNull(domain)) {
                return domain;
            }
        }

        // Fall back to Referer header
        String referer = request.getHeader(HttpConstants.HEADER_REFERER);
        if (Objects.nonNull(referer) && !referer.isBlank()) {
            String domain = parseHostFromUrl(referer);
            if (Objects.nonNull(domain)) {
                return domain;
            }
        }

        // Last resort: use Host header (for same-origin requests, preserve port)
        String host = request.getHeader("Host");
        if (Objects.nonNull(host) && !host.isBlank()) {
            return host;
        }

        return null;
    }

    /**
     * Parse host (domain) from a URL string, including port if present.
     *
     * @param url the URL string
     * @return the host with port (if present) or null if parsing fails
     */
    public static String parseHostFromUrl(String url) {
        if (Objects.isNull(url) || url.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port > 0) {
                return host + ":" + port;
            }
            return host;
        } catch (Exception e) {
            return null;
        }
    }
}
