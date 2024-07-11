package com.weatherapi.exception;

public class RateLimitExceededException extends WeatherApiException {
    public RateLimitExceededException(String message) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED, message);
    }
}