package com.weatherapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRateLimitExceededException() {
        RateLimitExceededException ex = new RateLimitExceededException("Rate limit exceeded");
        ProblemDetail problemDetail = handler.handleWeatherApiException(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), problemDetail.getStatus());
        assertEquals(ErrorCode.RATE_LIMIT_EXCEEDED.toString(), problemDetail.getTitle());
        assertEquals("Rate limit exceeded", problemDetail.getDetail());
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ProblemDetail problemDetail = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals(ErrorCode.INVALID_INPUT.toString(), problemDetail.getTitle());
        assertEquals("Invalid input", problemDetail.getDetail());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ProblemDetail problemDetail = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals(ErrorCode.UNKNOWN_ERROR.toString(), problemDetail.getTitle());
        assertEquals("An unexpected error occurred", problemDetail.getDetail());
    }


}

