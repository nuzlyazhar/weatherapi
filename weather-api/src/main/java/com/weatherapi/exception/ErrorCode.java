package com.weatherapi.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_API_KEY("WA-001"),
    RATE_LIMIT_EXCEEDED("WA-002"),
    INVALID_INPUT("WA-003"),
    EXTERNAL_API_ERROR("WA-004"),
    UNKNOWN_ERROR("WA-999");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

}
