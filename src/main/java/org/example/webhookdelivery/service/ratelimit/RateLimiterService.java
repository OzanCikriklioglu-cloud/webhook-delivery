package org.example.webhookdelivery.service.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-user in-memory rate limiter for event creation.
 * Each user gets an independent token bucket. Limits are configurable via application.yml.
 */
@Service
public class RateLimiterService {

    @Value("${webhook.rate-limit.capacity:20}")
    private int capacity;

    @Value("${webhook.rate-limit.refill-tokens:20}")
    private int refillTokens;

    @Value("${webhook.rate-limit.refill-seconds:60}")
    private int refillSeconds;

    private final ConcurrentHashMap<Long, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Returns true if the request is allowed, false if the rate limit is exceeded.
     */
    public boolean tryConsume(Long userId) {
        return buckets.computeIfAbsent(userId, this::newBucket).tryConsume(1);
    }

    private Bucket newBucket(Long userId) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofSeconds(refillSeconds))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
