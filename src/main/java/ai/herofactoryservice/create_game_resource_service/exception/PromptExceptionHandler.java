package ai.herofactoryservice.create_game_resource_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ai.herofactoryservice.create_game_resource_service.exception.GlobalExceptionHandler.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class PromptExceptionHandler {

    @ExceptionHandler(PromptException.class)
    public ResponseEntity<ErrorResponse> handlePromptException(PromptException e) {
        log.error("Prompt error occurred: {}", e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .code(e.getErrorCode())
                .message(e.getMessage())
                .details(e.getDetails())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error: ", e);

        ErrorResponse response = ErrorResponse.builder()
                .code(PromptErrorCode.PROMPT_SYSTEM_ERROR.getCode())
                .message("시스템 오류가 발생했습니다.")
                .details(e.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}