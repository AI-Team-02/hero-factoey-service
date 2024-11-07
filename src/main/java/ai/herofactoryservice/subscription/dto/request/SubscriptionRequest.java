package ai.herofactoryservice.subscription.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    @NotNull
    private String memberId;

    @NotNull
    private Long planId;

    @NotNull
    private String planName;

    @NotNull
    private Long amount;

    @NotNull
    private String billingCycle;  // MONTHLY, YEARLY

    @NotNull
    private Integer billingPeriod;

    private String successUrl;
    private String failUrl;
    private String cancelUrl;
}