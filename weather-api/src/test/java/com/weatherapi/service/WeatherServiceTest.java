package com.weatherapi.service;

import com.weatherapi.exception.InvalidApiKeyException;
import com.weatherapi.exception.RateLimitExceededException;
import com.weatherapi.model.WeatherReport;
import com.weatherapi.repository.WeatherReportRepository;
import com.weatherapi.util.ApiKeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class WeatherServiceTest {

    @Mock
    private WeatherReportRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiKeyManager apiKeyManager;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        weatherService = new WeatherService(repository, restTemplate, apiKeyManager);
        ReflectionTestUtils.setField(weatherService, "apiUrl", "http://api.openweathermap.org/data/2.5/weather");
    }

    @Test
    void getWeatherReport_ValidApiKey_ReturnsWeatherReport() {
        String city = "London";
        String country = "UK";
        String apiKey = "valid_key";

        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(true);
        when(apiKeyManager.allowRequest(apiKey)).thenReturn(true);
        when(repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country))
                .thenReturn(Optional.empty());

        Map<String, Object> weatherData = Map.of(
                "weather", List.of(Map.of("description", "Cloudy"))
        );
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(weatherData);

        // Mock the save operation
        when(repository.save(any(WeatherReport.class))).thenAnswer(invocation -> {
            WeatherReport reportToSave = invocation.getArgument(0);
            return new WeatherReport(1L, reportToSave.city(), reportToSave.country(),
                    reportToSave.description(), reportToSave.timestamp());
        });

        WeatherReport report = weatherService.getWeatherReport(city, country, apiKey);

        assertNotNull(report);
        assertEquals(city, report.city());
        assertEquals(country, report.country());
        assertEquals("Cloudy", report.description());
    }

    @Test
    void getWeatherReport_InvalidApiKey_ThrowsIllegalArgumentException() {
        String apiKey = "invalid_key";
        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(false);

        assertThrows(InvalidApiKeyException.class,
                () -> weatherService.getWeatherReport("London", "UK", apiKey));
    }

    @Test
    void getWeatherReport_RateLimitExceeded_ThrowsRateLimitExceededException() {
        String apiKey = "valid_key";
        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(true);
        when(apiKeyManager.allowRequest(apiKey)).thenReturn(false);

        assertThrows(RateLimitExceededException.class,
                () -> weatherService.getWeatherReport("London", "UK", apiKey));
    }

    @Test
    void getWeatherReport_CachedReport_ReturnsCachedReport() {
        String city = "Paris";
        String country = "FR";
        String apiKey = "valid_key";
        long currentTime = Instant.now().getEpochSecond();
        WeatherReport cachedReport = new WeatherReport(1L, city, country, "Sunny", currentTime - 1800); // 30 minutes old

        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(true);
        when(apiKeyManager.allowRequest(apiKey)).thenReturn(true);
        when(repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country))
                .thenReturn(Optional.of(cachedReport));

        WeatherReport report = weatherService.getWeatherReport(city, country, apiKey);

        assertNotNull(report);
        assertEquals(cachedReport, report);
    }

    @Test
    void getWeatherReport_InvalidApiResponse_ThrowsRuntimeException() {
        String city = "InvalidCity";
        String country = "IC";
        String apiKey = "valid_key";

        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(true);
        when(apiKeyManager.allowRequest(apiKey)).thenReturn(true);
        when(repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country))
                .thenReturn(Optional.empty());

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> weatherService.getWeatherReport(city, country, apiKey));
    }

    @Test
    void getWeatherReport_ExternalApiReturnsNull_ThrowsRuntimeException() {
        String city = "London";
        String country = "UK";
        String apiKey = "valid_key";

        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(true);
        when(apiKeyManager.allowRequest(apiKey)).thenReturn(true);
        when(repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> weatherService.getWeatherReport(city, country, apiKey));
    }

    @Test
    void getWeatherReport_ExternalApiReturnsInvalidData_ThrowsRuntimeException() {
        String city = "London";
        String country = "UK";
        String apiKey = "valid_key";

        when(apiKeyManager.isValidApiKey(apiKey)).thenReturn(true);
        when(apiKeyManager.allowRequest(apiKey)).thenReturn(true);
        when(repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of("invalid", "data"));

        assertThrows(RuntimeException.class, () -> weatherService.getWeatherReport(city, country, apiKey));
    }
}