package ai.herofactoryservice.create_game_resource_service.model.dto;

import ai.herofactoryservice.create_game_resource_service.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private PaymentStatus status;
    private String errorMessage;
    private String tid;
    private String nextRedirectPcUrl;
    private String nextRedirectMobileUrl;
}

