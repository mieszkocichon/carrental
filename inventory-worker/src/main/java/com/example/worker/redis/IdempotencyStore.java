package com.example.worker.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyStore {
    private final StringRedisTemplate redis;
    public IdempotencyStore(StringRedisTemplate redis) { this.redis = redis; }

    private static String key(String reservationId) {
        return "idemp:worker:reservation:" + reservationId;
    }

    /** Returns true if REGISTERED for the first time; false if already exists (duplicate). */
    public boolean firstProcess(String reservationId) {
        Boolean ok = redis.opsForValue().setIfAbsent(key(reservationId), "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(ok);
    }
}
