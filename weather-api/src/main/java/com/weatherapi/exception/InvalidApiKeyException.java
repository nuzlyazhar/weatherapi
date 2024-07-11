package com.weatherapi.exception;

public class InvalidApiKeyException extends WeatherApiException {
    public InvalidApiKeyException(String message) {
        super(ErrorCode.INVALID_API_KEY, message);
    }
}
