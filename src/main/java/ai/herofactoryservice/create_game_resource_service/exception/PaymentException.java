package ai.herofactoryservice.create_game_resource_service.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final PaymentErrorCode errorCode;
    private final String details;

    public PaymentException(String message) {
        super(message);
        this.errorCode = PaymentErrorCode.PAYMENT_SYSTEM_ERROR;
        this.details = null;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = PaymentErrorCode.PAYMENT_SYSTEM_ERROR;
        this.details = cause.getMessage();
    }

    public PaymentException(PaymentErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public PaymentException(PaymentErrorCode errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public PaymentException(PaymentErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = cause.getMessage();
    }

    public String getErrorCode() {
        return this.errorCode.getCode();
    }
}