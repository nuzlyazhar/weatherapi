package com.weatherapi.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(WeatherApiException.class)
    public ProblemDetail handleWeatherApiException(WeatherApiException ex) {
        HttpStatus status = determineHttpStatus(ex);
        return createProblemDetail(ex.getErrorCode(), status, ex.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ProblemDetail handleValidationExceptions(Exception ex) {
        return createProblemDetail(ErrorCode.INVALID_INPUT, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = createProblemDetail(ErrorCode.INVALID_INPUT, HttpStatus.BAD_REQUEST,
                "Validation error: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = createProblemDetail(ErrorCode.INVALID_INPUT, HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing");
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        return createProblemDetail(ErrorCode.UNKNOWN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private HttpStatus determineHttpStatus(WeatherApiException ex) {
        return switch (ex.getErrorCode()) {
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case INVALID_API_KEY -> HttpStatus.UNAUTHORIZED;
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case EXTERNAL_API_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ProblemDetail createProblemDetail(ErrorCode errorCode, HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(errorCode.name());
        problemDetail.setProperty("errorCode", errorCode.getCode());
        return problemDetail;
    }
}