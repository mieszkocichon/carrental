package com.example.worker.redis;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Simple availability cache. In real it works with read-modelem/bazą. */
@Component
public class AvailabilityCache {
    private final StringRedisTemplate redis;
    public AvailabilityCache(StringRedisTemplate redis) { this.redis = redis; }

    private static String key(String vehicleId) { return "availability:vehicle:" + vehicleId; }

    /** Store availability (np. "AVAILABLE" / "UNAVAILABLE:until:ISO"). TTL skraca stary stan. */
    public void put(String vehicleId, String state, Duration ttl) {
        redis.opsForValue().set(key(vehicleId), state, ttl);
    }

    public Optional<String> get(String vehicleId) {
        return Optional.ofNullable(redis.opsForValue().get(key(vehicleId)));
    }
}
