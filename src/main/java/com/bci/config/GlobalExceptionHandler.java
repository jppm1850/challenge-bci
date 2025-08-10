package com.bci.exception;

import com.bci.model.ErrorResponseDTO;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleValidationErrors(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO response = new ErrorResponseDTO(Collections.singletonList(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.BAD_REQUEST.value(),
                        message
                )
        ));

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(UserExistsException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleUserExists(UserExistsException ex) {
        log.error("User already exists: {}", ex.getMessage());

        ErrorResponseDTO response = new ErrorResponseDTO(Collections.singletonList(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                )
        ));

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());

        ErrorResponseDTO response = new ErrorResponseDTO(Collections.singletonList(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                )
        ));

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler({JwtException.class, InvalidTokenException.class})
    public Mono<ResponseEntity<ErrorResponseDTO>> handleJwtException(Exception ex) {
        log.error("JWT error: {}", ex.getMessage());

        ErrorResponseDTO response = new ErrorResponseDTO(Collections.singletonList(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid or expired token"
                )
        ));

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleValidationException(ValidationException ex) {
        log.error("Validation exception: {}", ex.getMessage());

        ErrorResponseDTO response = new ErrorResponseDTO(Collections.singletonList(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                )
        ));

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponseDTO response = new ErrorResponseDTO(Collections.singletonList(
                new ErrorResponseDTO.Error(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred"
                )
        ));

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}