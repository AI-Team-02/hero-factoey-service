package com.herofactory.subscription.dto.response;

import com.herofactory.subscription.entity.enums.BillingCycle;
import com.herofactory.subscription.entity.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String subscriptionId;
    private String memberId;
    private Long planId;
    private String planName;
    private Long amount;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextPaymentDate;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    // 추가적인 플랜 정보
    private List<String> planFeatures;
    private Integer yearlyDiscountPercent;

    // 결제 정보
    private LocalDateTime lastPaymentDate;
    private Long lastPaymentAmount;

    // 메타데이터
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 구독 상태 편의 메서드들
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public boolean isCancelled() {
        return status == SubscriptionStatus.CANCELLED;
    }

    public boolean isExpired() {
        return status == SubscriptionStatus.EXPIRED;
    }

    public boolean needsPayment() {
        return status == SubscriptionStatus.PAYMENT_PENDING ||
                status == SubscriptionStatus.PAYMENT_FAILED;
    }

    // 남은 구독 기간 계산
    public long getRemainingDays() {
        if (endDate == null) {
            return -1; // 무기한 구독
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }
}