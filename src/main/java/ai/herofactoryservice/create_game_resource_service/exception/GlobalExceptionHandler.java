package ai.herofactoryservice.create_game_resource_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PromptException.class)
    public ResponseEntity<ErrorResponse> handlePromptException(PromptException ex) {
        log.error("Prompt processing error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getDetails()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
        log.error("Payment processing error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getDetails()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(
                new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {
        private String code;
        private String message;
        private String details;
    }
}