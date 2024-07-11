package com.weatherapi.util;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ApiKeyManager {
    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final String[] API_KEYS = {"API_KEY_1", "API_KEY_2", "API_KEY_3", "API_KEY_4", "API_KEY_5"};

    private final Map<String, ApiKeyInfo> apiKeyInfoMap;
    private final Clock clock;

    public ApiKeyManager() {
        this(Clock.systemDefaultZone());
    }

    // Constructor for testing
    ApiKeyManager(Clock clock) {
        this.clock = clock;
        apiKeyInfoMap = new ConcurrentHashMap<>();
        for (String key : API_KEYS) {
            apiKeyInfoMap.put(key, new ApiKeyInfo());
        }
    }

    public boolean isValidApiKey(String apiKey) {
        return apiKeyInfoMap.containsKey(apiKey);
    }

    public boolean allowRequest(String apiKey) {
        ApiKeyInfo apiKeyInfo = apiKeyInfoMap.get(apiKey);
        if (apiKeyInfo == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime resetTime = apiKeyInfo.lastReset.plusHours(1);

        if (now.isAfter(resetTime)) {
            apiKeyInfo.lastReset = now;
            apiKeyInfo.requestCount.set(1);
            return true;
        }

        return apiKeyInfo.requestCount.incrementAndGet() <= MAX_REQUESTS_PER_HOUR;
    }

    private class ApiKeyInfo {
        private LocalDateTime lastReset = LocalDateTime.now(clock).minusHours(1);
        private final AtomicInteger requestCount = new AtomicInteger(0);
    }
}