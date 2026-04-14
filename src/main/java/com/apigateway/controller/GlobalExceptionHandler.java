package com.apigateway.controller;

import com.apigateway.dto.ErrorDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorDto>> handleValidationException(WebExchangeBindException ex, ServerHttpRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        log.error("Validation failed for: {}", request.getURI());
        return Mono.just(buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ErrorDto>> handleNoResourceFound(NoResourceFoundException ex, ServerHttpRequest request) {
        return Mono.just(buildResponse(HttpStatus.NOT_FOUND, "Resource not found", request, null));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorDto>> handleWebClientResponseException(WebClientResponseException ex, ServerHttpRequest request) {
        log.error("Service responded with error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());

        String message = (ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
                ? "Service is temporarily unavailable. Please try again later."
                : "Error from downstream service";

        return Mono.just(buildResponse(
                (HttpStatus) ex.getStatusCode(),
                message,
                request,
                null
        ));
    }

    @ExceptionHandler(WebClientRequestException.class)
    public Mono<ResponseEntity<ErrorDto>> handleWebClientRequestException(WebClientRequestException ex, ServerHttpRequest request) {
        log.error("Service is down: {}", ex.getMessage());

        return Mono.just(buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Downstream service is temporarily unavailable. Please try again later.",
                request,
                null
        ));
    }

    @ExceptionHandler({
            SignatureException.class,
            ExpiredJwtException.class,
            MalformedJwtException.class,
            UnsupportedJwtException.class
    })
    public Mono<ResponseEntity<ErrorDto>> handleJwtExceptions(Exception ex, ServerHttpRequest request) {
        log.error("JWT Error: {}", ex.getMessage());
        return Mono.just(buildResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token", request, null));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorDto>> handleGlobalException(Exception ex, ServerHttpRequest request) {
        log.error("Unexpected error occurred: ", ex);
        return Mono.just(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", request, null));
    }

    private ResponseEntity<ErrorDto> buildResponse(HttpStatus status, String message, ServerHttpRequest request, Map<String, String> validationErrors) {
        ErrorDto errorDto = ErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getURI().getPath())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorDto, status);
    }
}