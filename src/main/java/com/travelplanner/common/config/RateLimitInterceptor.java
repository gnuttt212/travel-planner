package com.travelplanner.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Rate Limiter dung Bucket4j.
 * Trong moi truong prod, Bucket4j co the tich hop voi Redis (thong qua JCache / Lettuce) 
 * de limit tren nhieu instances. O day (demo), ta dung ConcurrentMap local.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${rate-limit.requests-per-minute:5}")
    private int requestsPerMinute;

    private final ConcurrentMap<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Chi apply rate limit cho cac endpoint ton kem (Gemini API, Mapbox TSP)
        if (uri.startsWith("/api/v1/itineraries/generate") || uri.startsWith("/api/v1/routes/optimize")) {
            
            // Xac dinh user theo IP hoac user_id tu JWT
            String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : request.getRemoteAddr();
            String key = uri + "_" + username;
            
            Bucket bucket = cache.computeIfAbsent(key, this::newBucket);
            
            if (bucket.tryConsume(1)) {
                return true; // OK
            } else {
                log.warn("Rate limit exceeded for user: {} on endpoint: {}", username, uri);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests. Please try again later.");
                return false;
            }
        }
        
        return true;
    }

    private Bucket newBucket(String key) {
        // Cho phep X requests moi phut
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
