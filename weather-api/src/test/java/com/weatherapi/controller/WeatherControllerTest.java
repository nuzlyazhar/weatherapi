package com.weatherapi.controller;

import com.weatherapi.exception.ErrorCode;
import com.weatherapi.exception.RateLimitExceededException;
import com.weatherapi.model.WeatherReport;
import com.weatherapi.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getWeatherReport_ValidRequest_ReturnsWeatherReport() throws Exception {
        WeatherReport report = new WeatherReport(1L, "London", "UK", "Cloudy", System.currentTimeMillis() / 1000);
        when(weatherService.getWeatherReport(anyString(), anyString(), anyString())).thenReturn(report);

        mockMvc.perform(get("/api/weather")
                        .param("city", "London")
                        .param("country", "UK")
                        .header("X-API-Key", "valid_key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.country").value("UK"))
                .andExpect(jsonPath("$.description").value("Cloudy"));
    }

    @Test
    void getWeatherReport_MissingParams_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .header("X-API-Key", "valid_key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeatherReport_MissingApiKey_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .param("city", "London")
                        .param("country", "UK")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeatherReport_RateLimitExceeded_ReturnsTooManyRequests() throws Exception {
        when(weatherService.getWeatherReport(anyString(), anyString(), anyString()))
                .thenThrow(new RateLimitExceededException("Rate limit exceeded"));

        mockMvc.perform(get("/api/weather")
                        .param("city", "London")
                        .param("country", "UK")
                        .header("X-API-Key", "valid_key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.title").value(ErrorCode.RATE_LIMIT_EXCEEDED.toString()));
    }

    @Test
    void getWeatherReport_InternalServerError_ReturnsInternalServerError() throws Exception {
        when(weatherService.getWeatherReport(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Internal Server Error"));

        mockMvc.perform(get("/api/weather")
                        .param("city", "London")
                        .param("country", "UK")
                        .header("X-API-Key", "valid_key")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value(ErrorCode.UNKNOWN_ERROR.toString()));
    }
}