package ai.herofactoryservice.common.exception;

import lombok.Getter;

@Getter
public class PromptException extends RuntimeException {
    private final String errorCode;
    private final String details;

    public PromptException(String message) {
        super(message);
        this.errorCode = "PROMPT_ERROR";
        this.details = null;
    }

    public PromptException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PROMPT_ERROR";
        this.details = cause.getMessage();
    }

    public PromptException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public PromptException(String message, String errorCode, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}