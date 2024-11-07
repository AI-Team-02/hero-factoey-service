package ai.herofactoryservice.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequest {
    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}