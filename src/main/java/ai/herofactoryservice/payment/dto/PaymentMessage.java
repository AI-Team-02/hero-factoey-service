package ai.herofactoryservice.payment.dto;

import ai.herofactoryservice.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {
    private String paymentId;
    private Long shopItemId;
    private String memberId;
    private Long amount;
    private PaymentStatus status;
}