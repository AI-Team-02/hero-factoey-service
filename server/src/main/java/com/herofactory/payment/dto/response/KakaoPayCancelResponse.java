package com.herofactory.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoPayCancelResponse {
    private String aid;            // 요청 고유 번호
    private String tid;            // 결제 고유 번호
    private String cid;            // 가맹점 코드
    private String status;         // 결제 상태

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 가맹점 주문 번호

    @JsonProperty("partner_user_id")
    private String partnerUserId;  // 가맹점 회원 ID

    @JsonProperty("payment_method_type")
    private String paymentMethodType; // 결제 수단

    private Amount amount;         // 결제 금액 정보
    private ApprovedCancelAmount approved_cancel_amount; // 이번 요청으로 취소된 금액
    private CanceledAmount canceled_amount; // 누계 취소 금액
    private CancelAvailableAmount cancel_available_amount; // 남은 취소 가능 금액

    @JsonProperty("item_name")
    private String itemName;       // 상품명

    @JsonProperty("created_at")
    private String createdAt;      // 결제 준비 요청 시간

    @JsonProperty("approved_at")
    private String approvedAt;     // 결제 승인 시간

    @JsonProperty("canceled_at")
    private String canceledAt;     // 결제 취소 시간

    // 금액 정보를 위한 중첩 클래스들
    @Data
    public static class Amount {
        private Integer total;     // 총 결제 금액
        private Integer tax_free;  // 비과세 금액
        private Integer vat;       // 부가세 금액
        private Integer point;     // 사용한 포인트 금액
        private Integer discount;  // 할인 금액
    }

    @Data
    public static class ApprovedCancelAmount {
        private Integer total;     // 이번 요청으로 취소된 총 금액
        private Integer tax_free;  // 이번 요청으로 취소된 비과세 금액
        private Integer vat;       // 이번 요청으로 취소된 부가세 금액
        private Integer point;     // 이번 요청으로 취소된 포인트 금액
        private Integer discount;  // 이번 요청으로 취소된 할인 금액
    }

    @Data
    public static class CanceledAmount {
        private Integer total;     // 취소된 총 누적 금액
        private Integer tax_free;  // 취소된 비과세 누적 금액
        private Integer vat;       // 취소된 부가세 누적 금액
        private Integer point;     // 취소된 포인트 누적 금액
        private Integer discount;  // 취소된 할인 누적 금액
    }

    @Data
    public static class CancelAvailableAmount {
        private Integer total;     // 전체 취소 가능 금액
        private Integer tax_free;  // 취소 가능 비과세 금액
        private Integer vat;       // 취소 가능 부가세 금액
        private Integer point;     // 취소 가능 포인트 금액
        private Integer discount;  // 취소 가능 할인 금액
    }
}