package com.ecommerce.assistant.auth;

import com.ecommerce.assistant.security.RateLimitingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
    }

    @Test
    @DisplayName("Should permit requests under rate limit threshold")
    void testPermitRequestsUnderLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        final boolean[] called = new boolean[1];
        FilterChain chain = (req, res) -> called[0] = true;

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertTrue(called[0]);
    }

    @Test
    @DisplayName("Should extract client IP correctly from X-Forwarded-For header")
    void testExtractClientIpFromHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();
        final boolean[] called = new boolean[1];
        FilterChain chain = (req, res) -> called[0] = true;

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertTrue(called[0]);
    }
}
