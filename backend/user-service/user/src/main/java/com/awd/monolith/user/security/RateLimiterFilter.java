package com.awd.monolith.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * In-memory fixed-window rate limiter keyed by "IP | userId".
 */
@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int maxRequests;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimiterFilter(
            @Value("${rate-limit.enabled:true}") boolean enabled,
            @Value("${rate-limit.max-requests:100}") int maxRequests,
            @Value("${rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        long nowSeconds = Instant.now().getEpochSecond();

        WindowCounter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || nowSeconds >= existing.windowStart + windowSeconds) {
                return new WindowCounter(nowSeconds, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter.count.get() > maxRequests) {
            long retryAfter = Math.max(1L, (counter.windowStart + windowSeconds) - nowSeconds);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(String.format(
                    "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\",\"path\":\"%s\"}",
                    Instant.now(),
                    request.getRequestURI()
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        String ip = extractClientIp(request);
        String userId = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal && principal.id() != null) {
            userId = String.valueOf(principal.id());
        }
        return ip + "|" + userId;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    public void reset() {
        counters.clear();
    }

    private static final class WindowCounter {
        private final long windowStart;
        private final AtomicInteger count;

        private WindowCounter(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
