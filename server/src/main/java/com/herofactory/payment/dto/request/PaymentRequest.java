package com.herofactory.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long shopItemId;      // 상품 ID

    @NotNull(message = "회원 ID는 필수입니다.")
    private String memberId;        // 회원 ID

    @NotBlank(message = "상품명은 필수입니다.")
    private String itemName;      // 상품명

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 100, message = "결제 금액은 100원 이상이어야 합니다.")
    private Long amount;          // 결제 금액

    private String successUrl;    // 성공 시 리다이렉트 URL
    private String failUrl;       // 실패 시 리다이렉트 URL
    private String cancelUrl;     // 취소 시 리다이렉트 URL

    // 기본 URL 값 제공
    public String getSuccessUrl() {
        return successUrl != null ? successUrl : "http://localhost:8080/payment/success";
    }

    public String getFailUrl() {
        return failUrl != null ? failUrl : "http://localhost:8080/payment/fail";
    }

    public String getCancelUrl() {
        return cancelUrl != null ? cancelUrl : "http://localhost:8080/payment/cancel";
    }
}