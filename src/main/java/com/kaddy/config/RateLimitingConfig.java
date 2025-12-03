package com.kaddy.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofSeconds(3)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(30).refillIntervally(30, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket createBatchBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(20).refillIntervally(20, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }
}
