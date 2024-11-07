//package ai.herofactoryservice.subscription.service;
//
//import ai.herofactoryservice.common.exception.PaymentException;
//import ai.herofactoryservice.payment.dto.request.PaymentRequest;
//import ai.herofactoryservice.payment.dto.response.PaymentResponse;
//import ai.herofactoryservice.payment.entity.PaymentLog;
//import ai.herofactoryservice.payment.service.KakaoPayService;
//import ai.herofactoryservice.subscription.entity.*;
//import ai.herofactoryservice.payment.repository.PaymentLogRepository;
//import ai.herofactoryservice.subscription.entity.enums.SubscriptionStatus;
//import ai.herofactoryservice.subscription.repository.SubscriptionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class PaymentRetryService {
//    private final SubscriptionRepository subscriptionRepository;
//    private final PaymentLogRepository paymentLogRepository;
//    private final KakaoPayService kakaoPayService;
//    private final NotificationService notificationService;
//
//    @Value("${payment.retry.max-attempts}")
//    private int maxRetryAttempts;
//
//    @Value("${payment.retry.delay-minutes}")
//    private int retryDelayMinutes;
//
//    @Transactional
//    public void processPaymentRetry(String subscriptionId) {
//        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
//                .orElseThrow(() -> new PaymentException("구독 정보를 찾을 수 없습니다."));
//
//        PaymentRetryContext retryContext = new PaymentRetryContext(subscription);
//
//        if (!shouldRetryPayment(retryContext)) {
//            handleMaxRetriesExceeded(subscription);
//            return;
//        }
//
//        try {
//            executePaymentRetry(retryContext);
//        } catch (Exception e) {
//            handleRetryFailure(retryContext, e);
//        }
//    }
//
//    private boolean shouldRetryPayment(PaymentRetryContext context) {
//        // 재시도 횟수 체크
//        if (context.getRetryCount() >= maxRetryAttempts) {
//            log.info("Maximum retry attempts reached for subscription: {}",
//                    context.getSubscription().getSubscriptionId());
//            return false;
//        }
//
//        // 마지막 시도 이후 충분한 시간이 지났는지 체크
//        if (context.getLastRetryTime() != null) {
//            LocalDateTime nextRetryTime = context.getLastRetryTime()
//                    .plusMinutes(retryDelayMinutes);
//            if (LocalDateTime.now().isBefore(nextRetryTime)) {
//                log.info("Too soon to retry payment for subscription: {}",
//                        context.getSubscription().getSubscriptionId());
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private void executePaymentRetry(PaymentRetryContext context) {
//        Subscription subscription = context.getSubscription();
//
//        // 결제 요청 생성
//        PaymentRequest paymentRequest = createRetryPaymentRequest(subscription);
//
//        // 결제 시도
//        PaymentResponse response = kakaoPayService.initiatePayment(paymentRequest);
//
//        // 성공 시 처리
//        handleRetrySuccess(subscription, response);
//
//        // 로그 기록
//        saveRetryLog(subscription, "SUCCESS", null);
//    }
//
//    private void handleRetrySuccess(Subscription subscription, PaymentResponse response) {
//        subscription.setStatus(SubscriptionStatus.ACTIVE);
//        subscription.calculateNextPaymentDate();
//        subscription.setUpdatedAt(LocalDateTime.now());
//        subscriptionRepository.save(subscription);
//
//        notificationService.sendPaymentRetrySuccess(subscription);
//    }
//
//    private void handleRetryFailure(PaymentRetryContext context, Exception e) {
//        Subscription subscription = context.getSubscription();
//
//        // 로그 기록
//        saveRetryLog(subscription, "FAILED", e.getMessage());
//
//        // 다음 재시도 예약 또는 최종 실패 처리
//        if (context.getRetryCount() >= maxRetryAttempts - 1) {
//            handleMaxRetriesExceeded(subscription);
//        } else {
//            scheduleNextRetry(subscription);
//        }
//
//        notificationService.sendPaymentRetryFailure(subscription);
//    }
//
//    private void handleMaxRetriesExceeded(Subscription subscription) {
//        subscription.setStatus(SubscriptionStatus.EXPIRED);
//        subscription.setEndDate(LocalDateTime.now());
//        subscription.setUpdatedAt(LocalDateTime.now());
//        subscriptionRepository.save(subscription);
//
//        notificationService.sendSubscriptionExpired(subscription);
//
//        // 이벤트 발행
//        eventPublisher.publishSubscriptionExpiredEvent(subscription);
//    }
//
//    private void saveRetryLog(Subscription subscription, String status, String errorMessage) {
//        PaymentLog log = PaymentLog.builder()
//                .subscription(subscription)
//                .logType("PAYMENT_RETRY")
//                .status(status)
//                .errorMessage(errorMessage)
//                .createdAt(LocalDateTime.now())
//                .build();
//        paymentLogRepository.save(log);
//    }
//
//    private static class PaymentRetryContext {
//        private final Subscription subscription;
//        private final int retryCount;
//        private final LocalDateTime lastRetryTime;
//
//        PaymentRetryContext(Subscription subscription) {
//            this.subscription = subscription;
//            List<PaymentLog> retryLogs = subscription.getPaymentLogs().stream()
//                    .filter(log -> "PAYMENT_RETRY".equals(log.getLogType()))
//                    .toList();
//            this.retryCount = retryLogs.size();
//            this.lastRetryTime = retryLogs.isEmpty() ? null :
//                    retryLogs.get(retryLogs.size() - 1).getCreatedAt();
//        }
//
//        public Subscription getSubscription() {
//            return subscription;
//        }
//
//        public int getRetryCount() {
//            return retryCount;
//        }
//
//        public LocalDateTime getLastRetryTime() {
//            return lastRetryTime;
//        }
//    }
//}
