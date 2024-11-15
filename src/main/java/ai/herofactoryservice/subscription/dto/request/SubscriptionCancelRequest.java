package ai.herofactoryservice.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCancelRequest {
    @NotBlank(message = "취소 사유는 필수입니다")
    @Size(max = 500, message = "취소 사유는 500자를 초과할 수 없습니다")
    private String reason;
}