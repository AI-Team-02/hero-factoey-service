package com.herofactory.subscription.dto.request;

import com.herofactory.subscription.entity.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    @NotNull(message = "회원 ID는 필수입니다.")
    private String memberId;

    @NotNull(message = "구독 플랜 ID는 필수입니다.")
    private Long planId;

    @NotNull(message = "결제 주기는 필수입니다.")
    private BillingCycle billingCycle;

    private String successUrl;
    private String failUrl;
    private String cancelUrl;

    // 기본 URL 값 제공
    public String getSuccessUrl() {
        return successUrl != null ? successUrl : "http://localhost:8080/subscriptions/success";
    }

    public String getFailUrl() {
        return failUrl != null ? failUrl : "http://localhost:8080/subscriptions/fail";
    }

    public String getCancelUrl() {
        return cancelUrl != null ? cancelUrl : "http://localhost:8080/subscriptions/cancel";
    }
}