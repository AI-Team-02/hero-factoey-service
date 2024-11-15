package ai.herofactoryservice.payment.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY("결제 준비", "결제 준비 상태"),
    PENDING("결제 대기", "결제 승인 대기 상태"),
    IN_PROGRESS("결제 진행 중", "결제가 진행 중인 상태"),
    COMPLETED("결제 완료", "결제가 성공적으로 완료된 상태"),
    FAILED("결제 실패", "결제 처리 중 오류가 발생한 상태"),
    CANCELED("결제 취소", "결제가 전액 취소된 상태"),
    PARTIAL_CANCELED("부분 취소", "결제가 일부 취소된 상태"),
    EXPIRED("만료됨", "결제 요청이 만료된 상태"),
    REFUNDED("환불됨", "결제가 환불된 상태");

    private final String description;
    private final String detailMessage;

    PaymentStatus(String description, String detailMessage) {
        this.description = description;
        this.detailMessage = detailMessage;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == CANCELED ||
                this == PARTIAL_CANCELED || this == EXPIRED || this == REFUNDED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean canCancel() {
        return this == COMPLETED;
    }

    public boolean canRefund() {
        return this == COMPLETED || this == PARTIAL_CANCELED;
    }

    public boolean isInProgress() {
        return this == READY || this == PENDING || this == IN_PROGRESS;
    }
}