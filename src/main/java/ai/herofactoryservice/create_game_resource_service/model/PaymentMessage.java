package ai.herofactoryservice.create_game_resource_service.model;

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
    private Long memberId;
    private Long amount;
    private PaymentStatus status;
}