package com.weatherapi.controller;

import com.weatherapi.model.WeatherReport;
import com.weatherapi.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherReport> getWeatherDescription(
            @RequestParam @NotBlank(message = "City is required") String city,
            @RequestParam @NotBlank(message = "Country is required") String country,
            @RequestHeader("X-API-Key") @NotBlank(message = "API key is required") String apiKey) {
        WeatherReport report = weatherService.getWeatherReport(city, country, apiKey);
        return ResponseEntity.ok(report);
    }
}