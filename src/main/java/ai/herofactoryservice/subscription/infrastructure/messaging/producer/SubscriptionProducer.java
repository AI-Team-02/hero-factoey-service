//package ai.herofactoryservice.subscription.infrastructure.messaging.producer;
//
//import ai.herofactoryservice.subscription.entity.Subscription;
//import ai.herofactoryservice.subscription.service.event.SubscriptionEvent;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Component;
//
//// Event Publisher
//@Component
//@RequiredArgsConstructor
//public class SubscriptionProducer {
//    private final ApplicationEventPublisher eventPublisher;
//
//    public void publishSubscriptionCreatedEvent(Subscription subscription) {
//        eventPublisher.publishEvent(new SubscriptionEvent(this, subscription, "CREATED"));
//    }
//
//    public void publishSubscriptionRenewedEvent(Subscription subscription) {
//        eventPublisher.publishEvent(new SubscriptionEvent(this, subscription, "RENEWED"));
//    }
//
//    public void publishSubscriptionCancelledEvent(Subscription subscription) {
//        eventPublisher.publishEvent(new SubscriptionEvent(this, subscription, "CANCELLED"));
//    }
//
//    public void publishSubscriptionExpiredEvent(Subscription subscription) {
//        eventPublisher.publishEvent(new SubscriptionEvent(this, subscription, "EXPIRED"));
//    }
//
//    public void publishPaymentFailedEvent(Subscription subscription) {
//        eventPublisher.publishEvent(new SubscriptionEvent(this, subscription, "PAYMENT_FAILED"));
//    }
//}