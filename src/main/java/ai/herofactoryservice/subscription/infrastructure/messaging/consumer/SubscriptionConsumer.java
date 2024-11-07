//package ai.herofactoryservice.subscription.infrastructure.messaging.consumer;
//
//import ai.herofactoryservice.subscription.service.NotificationService;
//import ai.herofactoryservice.subscription.service.event.SubscriptionEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class SubscriptionConsumer {
//    private final NotificationService notificationService;
//    private final MetricsService metricsService;
//
//    @EventListener
//    public void handleSubscriptionEvent(SubscriptionEvent event) {
//        log.info("Handling subscription event: {} for subscription: {}",
//                event.getEventType(), event.getSubscription().getSubscriptionId());
//
//        // 메트릭스 업데이트
//        updateMetrics(event);
//
//        // 이벤트 타입별 처리
//        switch (event.getEventType()) {
//            case "CREATED" -> handleSubscriptionCreated(event);
//            case "RENEWED" -> handleSubscriptionRenewed(event);
//            case "CANCELLED" -> handleSubscriptionCancelled(event);
//            case "EXPIRED" -> handleSubscriptionExpired(event);
//            case "PAYMENT_FAILED" -> handlePaymentFailed(event);
//        }
//    }
//
//    private void updateMetrics(SubscriptionEvent event) {
//        try {
//            metricsService.recordSubscriptionEvent(
//                    event.getSubscription(),
//                    event.getEventType()
//            );
//        } catch (Exception e) {
//            log.error("Failed to update metrics for subscription event", e);
//        }
//    }
//
//    private void handleSubscriptionCreated(SubscriptionEvent event) {
//        // 구독 시작 처리
//        notificationService.sendWelcomeMessage(event.getSubscription());
//    }
//
//    private void handleSubscriptionRenewed(SubscriptionEvent event) {
//        // 구독 갱신 처리
//        notificationService.sendRenewalSuccess(event.getSubscription());
//    }
//
//    private void handleSubscriptionCancelled(SubscriptionEvent event) {
//        // 구독 취소 처리
//        notificationService.sendCancellationConfirmation(event.getSubscription());
//    }
//
//    private void handleSubscriptionExpired(SubscriptionEvent event) {
//        // 구독 만료 처리
//        notificationService.sendSubscriptionExpired(event.getSubscription());
//    }
//
//    private void handlePaymentFailed(SubscriptionEvent event) {
//        // 결제 실패 처리
//        notificationService.sendPaymentFailure(event.getSubscription());
//    }
//}
