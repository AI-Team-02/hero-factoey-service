package ai.herofactoryservice.create_game_resource_service.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final PaymentErrorCode errorCode;

    public PaymentException(String message) {
        super(message);
        this.errorCode = PaymentErrorCode.PAYMENT_SYSTEM_ERROR;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = PaymentErrorCode.PAYMENT_SYSTEM_ERROR;
    }

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PaymentException(PaymentErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}