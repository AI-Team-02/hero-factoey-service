package ai.herofactoryservice.common.exception;

import ai.herofactoryservice.create_game_resource_service.exception.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PromptException.class)
    public ResponseEntity<ErrorResponse> handlePromptException(PromptException ex) {
        log.error("Prompt processing error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code(ex.getErrorCode())
                        .message(ex.getMessage())
                        .details(ex.getDetails())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
        log.error("Payment processing error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code(ex.getErrorCode())
                        .message(ex.getMessage())
                        .details(ex.getDetails())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Authentication failed: {}", e.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code("AUTH_001")
                        .message(e.getMessage())
                        .details("Authentication failed")
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .build(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException e) {
        log.error("Rate limit exceeded: {}", e.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code("RATE_001")
                        .message(e.getMessage())
                        .details("Too many requests")
                        .status(HttpStatus.TOO_MANY_REQUESTS.value())
                        .build(),
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("Resource not found: {}", e.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code("RSC_001")
                        .message(e.getMessage())
                        .details(null)
                        .status(HttpStatus.NOT_FOUND.value())
                        .build(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException e) {
        log.error("Task not found: {}", e.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code("TASK_001")
                        .message(e.getMessage())
                        .details(null)
                        .status(HttpStatus.NOT_FOUND.value())
                        .build(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred: ", e);
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code("INTERNAL_ERROR")
                        .message("시스템 오류가 발생했습니다.")
                        .details(e.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Data
    @Builder
    public static class ErrorResponse {
        private String code;
        private String message;
        private String details;
        private int status;
    }
}