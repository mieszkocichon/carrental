package com.example.worker.redis;

import java.time.Duration;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class VehicleLockService {
    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> unlockScript;

    public VehicleLockService(StringRedisTemplate redis) {
        this.redis = redis;
        // Lua: remove lock only if value==token (to avoid deleting someone else's lock)
        String lua = "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end";
        this.unlockScript = new DefaultRedisScript<>(lua, Long.class);
    }

    private static String key(String vehicleId) {
        return "lock:vehicle:" + vehicleId;
    }

    /** Attempts to set a lock on vehicleId for a specified TTL. Returns a lock token or null. */
    public String tryLock(String vehicleId, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key(vehicleId), token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    /** Zdejmuje lock jeśli token pasuje. */
    public void unlock(String vehicleId, String token) throws DataAccessException {
        redis.execute(unlockScript, java.util.Collections.singletonList(key(vehicleId)), token);
    }
}
