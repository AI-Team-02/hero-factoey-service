package com.herofactory.subscription.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionMessage {
    private String subscriptionId;
    private String memberId;
    private String eventType;  // CREATED, RENEWED, CANCELLED, EXPIRED, PAYMENT_FAILED
    private Long planId;
    private String billingCycle;
    private Long amount;
}