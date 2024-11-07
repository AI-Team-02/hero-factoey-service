package ai.herofactoryservice.subscription.dto.response;

import ai.herofactoryservice.subscription.entity.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextPaymentDate;
    private String billingCycle;
    private String errorMessage;
}
