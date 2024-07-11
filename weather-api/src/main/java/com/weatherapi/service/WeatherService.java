package com.weatherapi.service;

import com.weatherapi.exception.ErrorCode;
import com.weatherapi.exception.InvalidApiKeyException;
import com.weatherapi.exception.RateLimitExceededException;
import com.weatherapi.exception.WeatherApiException;
import com.weatherapi.model.WeatherReport;
import com.weatherapi.repository.WeatherReportRepository;
import com.weatherapi.util.ApiKeyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherReportRepository repository;
    private final RestTemplate restTemplate;
    private final ApiKeyManager apiKeyManager;

    @Value("${openweathermap.api.url}")
    private String apiUrl;

    public WeatherReport getWeatherReport(String city, String country, String userApiKey) {
        if (!apiKeyManager.isValidApiKey(userApiKey)) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        if (!apiKeyManager.allowRequest(userApiKey)) {
            throw new RateLimitExceededException("Rate limit exceeded for API key: " + userApiKey);
        }

        return repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country)
                .filter(this::isReportFresh)
                .orElseGet(() -> fetchAndSaveWeatherReport(city, country, userApiKey));
    }

    private WeatherReport fetchAndSaveWeatherReport(String city, String country, String userApiKey) {
        String url = String.format("%s?q=%s,%s&appid=%s", apiUrl, city, country, userApiKey);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null) {
            throw new WeatherApiException(ErrorCode.EXTERNAL_API_ERROR, "No response from weather API");
        }

        Object weatherObj = response.get("weather");
        if (!(weatherObj instanceof List)) {
            throw new WeatherApiException(ErrorCode.EXTERNAL_API_ERROR, "Invalid weather data format");
        }

        List<?> weatherList = (List<?>) weatherObj;
        if (weatherList.isEmpty()) {
            throw new WeatherApiException(ErrorCode.EXTERNAL_API_ERROR, "Weather data is empty");
        }

        Object firstWeatherObj = weatherList.get(0);
        if (!(firstWeatherObj instanceof Map)) {
            throw new WeatherApiException(ErrorCode.EXTERNAL_API_ERROR, "Invalid weather data format");
        }

        Map<?, ?> weatherMap = (Map<?, ?>) firstWeatherObj;
        Object descriptionObj = weatherMap.get("description");
        if (!(descriptionObj instanceof String)) {
            throw new WeatherApiException(ErrorCode.EXTERNAL_API_ERROR, "Weather description is missing or invalid");
        }

        String description = (String) descriptionObj;
        if (description.isEmpty()) {
            throw new WeatherApiException(ErrorCode.EXTERNAL_API_ERROR, "Weather description is empty");
        }

        WeatherReport report = new WeatherReport(null, city, country, description, Instant.now().getEpochSecond());
        return repository.save(report);
    }

    private boolean isReportFresh(WeatherReport report) {
        return Instant.now().getEpochSecond() - report.timestamp() < 3600; // 1 hour
    }
}