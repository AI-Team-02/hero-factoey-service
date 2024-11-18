package com.herofactory.subscription.entity.enums;

public enum SubscriptionStatus {
    ACTIVE("활성"),
    CANCELLED("취소됨"),
    EXPIRED("만료됨"),
    SUSPENDED("정지됨"),
    PAYMENT_PENDING("결제 대기"),
    PAYMENT_FAILED("결제 실패");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canCancel() {
        return this == ACTIVE;
    }

    public boolean canReactivate() {
        return this == CANCELLED || this == EXPIRED || this == SUSPENDED;
    }

    public boolean requiresPayment() {
        return this == PAYMENT_PENDING || this == PAYMENT_FAILED;
    }
}
