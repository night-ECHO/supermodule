package com.adflex.tracking.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerRateLimitService {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int MAX_FAILS_PER_WINDOW = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, AttemptState> attemptByKey = new ConcurrentHashMap<>();

    public RateLimitDecision onFailedAttempt(String key) {
        Instant now = Instant.now();
        AttemptState state = attemptByKey.compute(key, (k, prev) -> {
            AttemptState next = prev != null ? prev : new AttemptState();
            if (next.lockedUntil != null && now.isBefore(next.lockedUntil)) {
                return next;
            }

            if (next.windowStart == null || now.isAfter(next.windowStart.plus(WINDOW))) {
                next.windowStart = now;
                next.failCount = 0;
            }

            next.failCount++;
            if (next.failCount >= MAX_FAILS_PER_WINDOW) {
                next.lockedUntil = now.plus(LOCK_DURATION);
            }
            return next;
        });

        if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
            return RateLimitDecision.locked(state.lockedUntil);
        }
        return RateLimitDecision.permit();
    }

    public RateLimitDecision checkLocked(String key) {
        AttemptState state = attemptByKey.get(key);
        Instant now = Instant.now();
        if (state != null && state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
            return RateLimitDecision.locked(state.lockedUntil);
        }
        return RateLimitDecision.permit();
    }

    public void clear(String key) {
        attemptByKey.remove(key);
    }

    private static class AttemptState {
        Instant windowStart;
        int failCount;
        Instant lockedUntil;
    }

    public record RateLimitDecision(boolean allowed, Instant lockedUntil) {
        public static RateLimitDecision permit() {
            return new RateLimitDecision(true, null);
        }

        public static RateLimitDecision locked(Instant until) {
            return new RateLimitDecision(false, until);
        }
    }
}
