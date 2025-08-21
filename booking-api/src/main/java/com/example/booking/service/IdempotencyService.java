package com.example.booking.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {
    private final StringRedisTemplate redis;

    public IdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(String idempKey) {
        return "idemp:api:" + idempKey;
    }

    /** Saves the mapping Idempotency-Key -> reservationId; if it already exists, returns the existing ID. */
    public String rememberOrGet(String idempotencyKey, String reservationId) {
        String k = key(idempotencyKey);
        Boolean set = redis.opsForValue().setIfAbsent(k, reservationId, Duration.ofHours(24));
        if (Boolean.TRUE.equals(set)) {
            return reservationId; // świeży zapis
        }
        return Optional.ofNullable(redis.opsForValue().get(k)).orElse(reservationId);
    }
}
