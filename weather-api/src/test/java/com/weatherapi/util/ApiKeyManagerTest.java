package com.weatherapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyManagerTest {

    private ApiKeyManager apiKeyManager;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.systemDefault());
        apiKeyManager = new ApiKeyManager(clock);
    }


    @Test
    void allowRequest_ResetAfterOneHour_AllowsNewRequests() {
        String apiKey = "API_KEY_1";
        for (int i = 0; i < 5; i++) {
            assertTrue(apiKeyManager.allowRequest(apiKey));
        }
        assertFalse(apiKeyManager.allowRequest(apiKey));

        // Move clock forward by 2 hours
        clock = Clock.fixed(Instant.parse("2023-01-01T02:00:00Z"), ZoneId.systemDefault());
        apiKeyManager = new ApiKeyManager(clock);

        // Should allow requests again
        assertTrue(apiKeyManager.allowRequest(apiKey));
    }

    @Test
    void isValidApiKey_ValidKey_ReturnsTrue() {
        assertTrue(apiKeyManager.isValidApiKey("API_KEY_1"));
        assertTrue(apiKeyManager.isValidApiKey("API_KEY_2"));
        assertTrue(apiKeyManager.isValidApiKey("API_KEY_3"));
        assertTrue(apiKeyManager.isValidApiKey("API_KEY_4"));
        assertTrue(apiKeyManager.isValidApiKey("API_KEY_5"));
    }

    @Test
    void isValidApiKey_InvalidKey_ReturnsFalse() {
        assertFalse(apiKeyManager.isValidApiKey("INVALID_KEY"));
    }

    @Test
    void allowRequest_ValidKeyWithinLimit_ReturnsTrue() {
        String apiKey = "API_KEY_1";
        for (int i = 0; i < 5; i++) {
            assertTrue(apiKeyManager.allowRequest(apiKey));
        }
    }

    @Test
    void allowRequest_ValidKeyExceedsLimit_ReturnsFalse() {
        String apiKey = "API_KEY_1";
        for (int i = 0; i < 5; i++) {
            assertTrue(apiKeyManager.allowRequest(apiKey));
        }
        assertFalse(apiKeyManager.allowRequest(apiKey));
    }

    @Test
    void allowRequest_InvalidKey_ReturnsFalse() {
        assertFalse(apiKeyManager.allowRequest("INVALID_KEY"));
    }

    @Test
    void allowRequest_ResetsCountAfterOneHour() {
        String apiKey = "API_KEY_1";
        for (int i = 0; i < 5; i++) {
            assertTrue(apiKeyManager.allowRequest(apiKey));
        }
        assertFalse(apiKeyManager.allowRequest(apiKey));

        // Move clock forward by exactly 1 hour
        clock = Clock.fixed(Instant.parse("2023-01-01T01:00:00Z"), ZoneId.systemDefault());
        apiKeyManager = new ApiKeyManager(clock);

        // Should reset count and allow request
        assertTrue(apiKeyManager.allowRequest(apiKey));
        assertTrue(apiKeyManager.allowRequest(apiKey)); // additional request to check the new count is 2
    }


}

