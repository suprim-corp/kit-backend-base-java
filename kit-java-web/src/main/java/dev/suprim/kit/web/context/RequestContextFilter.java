package dev.suprim.kit.web.context;

import dev.suprim.kit.core.UUIDUtils;
import dev.suprim.kit.web.HttpConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet filter that manages trace/request ID propagation for HTTP requests.
 * <p>
 * Behavior:
 * <ol>
 *   <li>Extracts trace ID from incoming {@code X-Trace-ID} header, or generates a new UUID v7 if absent</li>
 *   <li>Extracts request ID from {@code X-Request-ID} header, or generates a new UUID v7 if absent</li>
 *   <li>Stores both in {@link RequestContext} (ThreadLocal) and SLF4J MDC</li>
 *   <li>Sets {@code X-Trace-ID} and {@code X-Request-ID} response headers</li>
 *   <li>Cleans up ThreadLocal and MDC after request completes</li>
 * </ol>
 * <p>
 * Registration example (Spring Boot):
 * <pre>
 * &#64;Bean
 * public FilterRegistrationBean&lt;RequestContextFilter&gt; requestContextFilter() {
 *     FilterRegistrationBean&lt;RequestContextFilter&gt; registration = new FilterRegistrationBean&lt;&gt;(new RequestContextFilter());
 *     registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
 *     return registration;
 * }
 * </pre>
 */
public class RequestContextFilter implements Filter {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String traceId = resolveHeader(httpRequest, HttpConstants.HEADER_X_TRACE_ID);
            String requestId = resolveHeader(httpRequest, HttpConstants.HEADER_X_REQUEST_ID);

            // Store in ThreadLocal context
            RequestContext.set(RequestContext.KEY_TRACE_ID, traceId);
            RequestContext.set(RequestContext.KEY_REQUEST_ID, requestId);

            // Store in MDC for log correlation
            MDC.put(MDC_TRACE_ID, traceId);
            MDC.put(MDC_REQUEST_ID, requestId);

            // Set response headers so client can reference them
            httpResponse.setHeader(HttpConstants.HEADER_X_TRACE_ID, traceId);
            httpResponse.setHeader(HttpConstants.HEADER_X_REQUEST_ID, requestId);

            chain.doFilter(request, response);
        } finally {
            RequestContext.clear();
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /**
     * Extracts header value from request, or generates a new UUID v7 if absent/blank.
     */
    private String resolveHeader(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .filter(value -> !value.isBlank())
                .map(String::trim)
                .orElseGet(() -> UUIDUtils.v7().toString());
    }
}
