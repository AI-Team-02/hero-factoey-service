package ai.herofactoryservice.subscription.service;

import ai.herofactoryservice.subscription.entity.Subscription;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public void recordSubscriptionEvent(Subscription subscription, String eventType) {
        Counter.builder("subscription.events")
                .tags(Arrays.asList(
                        Tag.of("event_type", eventType),
                        Tag.of("plan_id", subscription.getPlan().getId().toString()),
                        Tag.of("billing_cycle", subscription.getBillingCycle().name())
                ))
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentEvent(Subscription subscription, String eventType, Long amount) {
        Counter.builder("subscription.payments")
                .tags(Arrays.asList(
                        Tag.of("event_type", eventType),
                        Tag.of("plan_id", subscription.getPlan().getId().toString()),
                        Tag.of("billing_cycle", subscription.getBillingCycle().name())
                ))
                .register(meterRegistry)
                .increment();

        // 결제 금액 기록
        meterRegistry.gauge("subscription.payment.amount",
                Arrays.asList(
                        Tag.of("plan_id", subscription.getPlan().getId().toString()),
                        Tag.of("billing_cycle", subscription.getBillingCycle().name())
                ),
                amount);
    }

    public void recordPlanChange(Subscription subscription, Long oldPlanId, Long newPlanId) {
        Counter.builder("subscription.plan_changes")
                .tags(Arrays.asList(
                        Tag.of("from_plan", oldPlanId.toString()),
                        Tag.of("to_plan", newPlanId.toString()),
                        Tag.of("billing_cycle", subscription.getBillingCycle().name())
                ))
                .register(meterRegistry)
                .increment();
    }

    public void recordSubscriptionCancellation(Subscription subscription, String reason) {
        Counter.builder("subscription.cancellations")
                .tags(Arrays.asList(
                        Tag.of("plan_id", subscription.getPlan().getId().toString()),
                        Tag.of("reason", reason),
                        Tag.of("billing_cycle", subscription.getBillingCycle().name())
                ))
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentFailure(Subscription subscription, String failureReason) {
        Counter.builder("subscription.payment.failures")
                .tags(Arrays.asList(
                        Tag.of("plan_id", subscription.getPlan().getId().toString()),
                        Tag.of("reason", failureReason),
                        Tag.of("billing_cycle", subscription.getBillingCycle().name())
                ))
                .register(meterRegistry)
                .increment();
    }
}