package dev.suprim.kit.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DomainUtilsTest {

	// ==================== parseHostFromUrl Tests ====================

	@Test
	void parseHostFromUrl_null_returnsNull() {
		assertNull(DomainUtils.parseHostFromUrl(null));
	}

	@Test
	void parseHostFromUrl_blank_returnsNull() {
		assertNull(DomainUtils.parseHostFromUrl("   "));
	}

	@Test
	void parseHostFromUrl_valid_returnsHost() {
		assertEquals("example.com", DomainUtils.parseHostFromUrl("https://example.com/path"));
	}

	@Test
	void parseHostFromUrl_withPort_includesPort() {
		assertEquals("localhost:3000", DomainUtils.parseHostFromUrl("http://localhost:3000/api"));
	}

	@Test
	void parseHostFromUrl_invalid_returnsNull() {
		assertNull(DomainUtils.parseHostFromUrl("not a url"));
	}

	@Test
	void parseHostFromUrl_localhost_works() {
		assertEquals("localhost", DomainUtils.parseHostFromUrl("http://localhost/"));
	}

	@Test
	void parseHostFromUrl_explicitPort_included() {
		// Explicit port is always included, even if standard
		assertEquals("example.com:443", DomainUtils.parseHostFromUrl("https://example.com:443/path"));
	}

	@Test
	void parseHostFromUrl_noExplicitPort_noPort() {
		// No explicit port means URI.getPort() returns -1
		assertEquals("example.com", DomainUtils.parseHostFromUrl("https://example.com/path"));
	}

	// ==================== extractDomain Tests ====================

	@Test
	void extractDomain_nullRequest_returnsNull() {
		assertNull(DomainUtils.extractDomain(null));
	}

	@Test
	void extractDomain_xForwardedHost_returnsFirst() {
		HttpServletRequest request = mockRequest(Map.of(
				"X-Forwarded-Host", "proxy.example.com"
		));

		assertEquals("proxy.example.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_xForwardedHost_multipleValues() {
		HttpServletRequest request = mockRequest(Map.of(
				"X-Forwarded-Host", "first.com, second.com, third.com"
		));

		assertEquals("first.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_origin_fallback() {
		HttpServletRequest request = mockRequest(Map.of(
				"Origin", "https://origin.example.com:8080"
		));

		assertEquals("origin.example.com:8080", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_referer_fallback() {
		HttpServletRequest request = mockRequest(Map.of(
				"Referer", "https://referer.example.com/page"
		));

		assertEquals("referer.example.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_host_fallback() {
		HttpServletRequest request = mockRequest(Map.of(
				"Host", "host.example.com:9000"
		));

		assertEquals("host.example.com:9000", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_noHeaders_returnsNull() {
		HttpServletRequest request = mockRequest(Map.of());

		assertNull(DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_priority_xForwardedHostFirst() {
		HttpServletRequest request = mockRequest(Map.of(
				"X-Forwarded-Host", "forwarded.com",
				"Origin", "https://origin.com",
				"Referer", "https://referer.com",
				"Host", "host.com"
		));

		assertEquals("forwarded.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_priority_originSecond() {
		HttpServletRequest request = mockRequest(Map.of(
				"Origin", "https://origin.com",
				"Referer", "https://referer.com",
				"Host", "host.com"
		));

		assertEquals("origin.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_priority_refererThird() {
		HttpServletRequest request = mockRequest(Map.of(
				"Referer", "https://referer.com",
				"Host", "host.com"
		));

		assertEquals("referer.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_blankXForwardedHost_fallsThrough() {
		HttpServletRequest request = mockRequest(Map.of(
				"X-Forwarded-Host", "   ",
				"Origin", "https://origin.com"
		));

		assertEquals("origin.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_blankOrigin_fallsThrough() {
		HttpServletRequest request = mockRequest(Map.of(
				"Origin", "   ",
				"Referer", "https://referer.com"
		));

		assertEquals("referer.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_invalidOriginUrl_fallsThrough() {
		HttpServletRequest request = mockRequest(Map.of(
				"Origin", "not a valid url",
				"Referer", "https://referer.com"
		));

		assertEquals("referer.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_blankReferer_fallsThrough() {
		HttpServletRequest request = mockRequest(Map.of(
				"Referer", "   ",
				"Host", "host.com"
		));

		assertEquals("host.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_invalidRefererUrl_fallsThrough() {
		HttpServletRequest request = mockRequest(Map.of(
				"Referer", "not a valid url",
				"Host", "host.com"
		));

		assertEquals("host.com", DomainUtils.extractDomain(request));
	}

	@Test
	void extractDomain_blankHost_returnsNull() {
		HttpServletRequest request = mockRequest(Map.of(
				"Host", "   "
		));

		assertNull(DomainUtils.extractDomain(request));
	}

	// ==================== Helper ====================

	private HttpServletRequest mockRequest(Map<String, String> headers) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		headers.forEach((key, value) -> when(request.getHeader(key)).thenReturn(value));
		return request;
	}
}
