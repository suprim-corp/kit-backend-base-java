package dev.suprim.kit.web.context;

import dev.suprim.kit.web.HttpConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestContextFilterTest {

    private RequestContextFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        filter = new RequestContextFilter();
    }

    @AfterEach
    void tearDown() throws Exception {
        RequestContext.clear();
        MDC.clear();
        mocks.close();
    }

    @Test
    void shouldExtractTraceIdFromHeader() throws IOException, ServletException {
        when(request.getHeader(HttpConstants.HEADER_X_TRACE_ID)).thenReturn("incoming-trace-id");
        when(request.getHeader(HttpConstants.HEADER_X_REQUEST_ID)).thenReturn("incoming-request-id");

        doAnswer(invocation -> {
            assertEquals("incoming-trace-id", RequestContext.getTraceId().orElseThrow());
            assertEquals("incoming-request-id", RequestContext.getRequestId().orElseThrow());
            assertEquals("incoming-trace-id", MDC.get(RequestContextFilter.MDC_TRACE_ID));
            assertEquals("incoming-request-id", MDC.get(RequestContextFilter.MDC_REQUEST_ID));
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(HttpConstants.HEADER_X_TRACE_ID, "incoming-trace-id");
        verify(response).setHeader(HttpConstants.HEADER_X_REQUEST_ID, "incoming-request-id");
    }

    @Test
    void shouldGenerateIdsWhenHeadersAbsent() throws IOException, ServletException {
        when(request.getHeader(HttpConstants.HEADER_X_TRACE_ID)).thenReturn(null);
        when(request.getHeader(HttpConstants.HEADER_X_REQUEST_ID)).thenReturn(null);

        doAnswer(invocation -> {
            assertTrue(RequestContext.getTraceId().isPresent());
            assertTrue(RequestContext.getRequestId().isPresent());
            assertNotEquals(
                    RequestContext.getTraceId().orElseThrow(),
                    RequestContext.getRequestId().orElseThrow()
            );
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(eq(HttpConstants.HEADER_X_TRACE_ID), anyString());
        verify(response).setHeader(eq(HttpConstants.HEADER_X_REQUEST_ID), anyString());
    }

    @Test
    void shouldGenerateIdsWhenHeadersBlank() throws IOException, ServletException {
        when(request.getHeader(HttpConstants.HEADER_X_TRACE_ID)).thenReturn("   ");
        when(request.getHeader(HttpConstants.HEADER_X_REQUEST_ID)).thenReturn("");

        doAnswer(invocation -> {
            assertTrue(RequestContext.getTraceId().isPresent());
            assertTrue(RequestContext.getRequestId().isPresent());
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);
    }

    @Test
    void shouldTrimHeaderValues() throws IOException, ServletException {
        when(request.getHeader(HttpConstants.HEADER_X_TRACE_ID)).thenReturn("  trace-with-spaces  ");
        when(request.getHeader(HttpConstants.HEADER_X_REQUEST_ID)).thenReturn("req-id");

        doAnswer(invocation -> {
            assertEquals("trace-with-spaces", RequestContext.getTraceId().orElseThrow());
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(HttpConstants.HEADER_X_TRACE_ID, "trace-with-spaces");
    }

    @Test
    void shouldCleanupContextAfterRequest() throws IOException, ServletException {
        when(request.getHeader(HttpConstants.HEADER_X_TRACE_ID)).thenReturn("trace-id");
        when(request.getHeader(HttpConstants.HEADER_X_REQUEST_ID)).thenReturn("request-id");

        filter.doFilter(request, response, chain);

        assertTrue(RequestContext.getTraceId().isEmpty());
        assertTrue(RequestContext.getRequestId().isEmpty());
        assertNull(MDC.get(RequestContextFilter.MDC_TRACE_ID));
        assertNull(MDC.get(RequestContextFilter.MDC_REQUEST_ID));
    }

    @Test
    void shouldCleanupContextEvenOnException() throws IOException, ServletException {
        when(request.getHeader(HttpConstants.HEADER_X_TRACE_ID)).thenReturn("trace-id");
        when(request.getHeader(HttpConstants.HEADER_X_REQUEST_ID)).thenReturn("request-id");
        doThrow(new ServletException("boom")).when(chain).doFilter(request, response);

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));

        assertTrue(RequestContext.getTraceId().isEmpty());
        assertNull(MDC.get(RequestContextFilter.MDC_TRACE_ID));
    }

    @Test
    void shouldPassThroughNonHttpRequest() throws IOException, ServletException {
        ServletRequest nonHttpRequest = mock(ServletRequest.class);
        ServletResponse nonHttpResponse = mock(ServletResponse.class);

        filter.doFilter(nonHttpRequest, nonHttpResponse, chain);

        verify(chain).doFilter(nonHttpRequest, nonHttpResponse);
        assertTrue(RequestContext.getTraceId().isEmpty());
        assertNull(MDC.get(RequestContextFilter.MDC_TRACE_ID));
    }

    @Test
    void shouldPassThroughWhenResponseNotHttp() throws IOException, ServletException {
        ServletResponse nonHttpResponse = mock(ServletResponse.class);

        filter.doFilter(request, nonHttpResponse, chain);

        verify(chain).doFilter(request, nonHttpResponse);
        assertTrue(RequestContext.getTraceId().isEmpty());
        assertNull(MDC.get(RequestContextFilter.MDC_TRACE_ID));
    }
}
