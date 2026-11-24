package com.ecommerce.assistant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 1000;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    private static class RequestCounter {
        final long resetTime;
        final AtomicInteger count;

        RequestCounter(long resetTime) {
            this.resetTime = resetTime;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        requestCounts.compute(clientIp, (key, current) -> {
            if (current == null || now > current.resetTime) {
                return new RequestCounter(now + 60000); // 1 minute window
            }
            current.count.incrementAndGet();
            return current;
        });

        RequestCounter counter = requestCounts.get(clientIp);
        if (counter != null && counter.count.get() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests. Please try again shortly.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
