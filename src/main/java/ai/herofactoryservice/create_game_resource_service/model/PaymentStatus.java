package ai.herofactoryservice.create_game_resource_service.model;

public enum PaymentStatus {
    READY("결제 준비"),
    PENDING("결제 대기"),
    IN_PROGRESS("결제 진행 중"),
    COMPLETED("결제 완료"),
    FAILED("결제 실패"),
    CANCELED("결제 취소"),
    PARTIAL_CANCELED("부분 취소"),
    EXPIRED("만료됨"),
    REFUNDED("환불됨");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
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
}