package com.agentdome.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for auth endpoints.
 * Blocks an IP for {@code BLOCK_DURATION_MS} after {@code MAX_ATTEMPTS} failed requests
 * within {@code WINDOW_MS}.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    /** Max attempts per window before blocking */
    private static final int MAX_ATTEMPTS = 10;
    /** Window duration in milliseconds */
    private static final long WINDOW_MS = 60_000; // 1 minute
    /** Block duration after exceeding limit */
    private static final long BLOCK_DURATION_MS = 5 * 60_000; // 5 minutes

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = getClientIp(request);
        String key = request.getRequestURI() + "|" + ip;

        long now = System.currentTimeMillis();
        AttemptWindow window = attempts.computeIfAbsent(key, k -> new AttemptWindow(now));

        synchronized (window) {
            // Check if currently blocked
            if (window.blockedUntil > 0) {
                if (now < window.blockedUntil) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                    log.warn("Rate limit blocked: key={}", key);
                    return false;
                }
                // Block expired, reset
                window.reset(now);
            }

            // Reset window if expired
            if (now - window.windowStart > WINDOW_MS) {
                window.reset(now);
            }

            window.count++;

            if (window.count > MAX_ATTEMPTS) {
                window.blockedUntil = now + BLOCK_DURATION_MS;
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"code\":429,\"message\":\"请求过于频繁，已被临时限制，请5分钟后再试\"}");
                log.warn("Rate limit exceeded, blocking: key={}, count={}", key, window.count);
                return false;
            }
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        // Check common proxy headers
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptWindow {
        long windowStart;
        int count;
        long blockedUntil;

        AttemptWindow(long start) {
            this.windowStart = start;
        }

        void reset(long start) {
            this.windowStart = start;
            this.count = 0;
            this.blockedUntil = 0;
        }
    }
}
