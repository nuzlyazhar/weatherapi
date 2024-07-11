package com.weatherapi.exception;

import lombok.Getter;

@Getter
public class WeatherApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public WeatherApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}