//package ai.herofactoryservice.subscription.scheduler;
//
//import ai.herofactoryservice.subscription.entity.Subscription;
//import ai.herofactoryservice.subscription.entity.enums.SubscriptionStatus;
//import ai.herofactoryservice.subscription.repository.SubscriptionRepository;
//import ai.herofactoryservice.subscription.service.NotificationService;
//import ai.herofactoryservice.subscription.service.SubscriptionService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class SubscriptionScheduler {
//    private final SubscriptionRepository subscriptionRepository;
//    private final SubscriptionService subscriptionService;
//    private final NotificationService notificationService;
//
//    // 매일 자정에 실행
//    @Scheduled(cron = "0 0 0 * * *")
//    @Transactional
//    public void processSubscriptionRenewals() {
//        log.info("Starting daily subscription renewal processing");
//        LocalDateTime now = LocalDateTime.now();
//
//        List<Subscription> dueSubscriptions = subscriptionRepository.findDueSubscriptions(now);
//        log.info("Found {} subscriptions due for renewal", dueSubscriptions.size());
//
//        for (Subscription subscription : dueSubscriptions) {
//            try {
//                processSubscriptionRenewal(subscription);
//            } catch (Exception e) {
//                log.error("Failed to process renewal for subscription: {}",
//                        subscription.getSubscriptionId(), e);
//                handleRenewalFailure(subscription);
//            }
//        }
//    }
//
//    // 매일 오전 9시에 실행 - 3일 후 갱신 예정인 구독에 대한 알림
//    @Scheduled(cron = "0 0 9 * * *")
//    public void sendRenewalReminders() {
//        LocalDateTime startDate = LocalDateTime.now().plusDays(3);
//        LocalDateTime endDate = startDate.plusDays(1);
//
//        List<Subscription> upcomingRenewals =
//                subscriptionRepository.findUpcomingRenewals(startDate, endDate);
//
//        for (Subscription subscription : upcomingRenewals) {
//            notificationService.sendRenewalReminder(subscription);
//        }
//    }
//
//    // 결제 실패한 구독에 대해 3일간 재시도 (4시간 간격)
//    @Scheduled(fixedDelay = 4, timeUnit = TimeUnit.HOURS)
//    @Transactional
//    public void retryFailedPayments() {
//        List<Subscription> failedSubscriptions =
//                subscriptionRepository.findByStatus(SubscriptionStatus.PAYMENT_FAILED);
//
//        for (Subscription subscription : failedSubscriptions) {
//            // 최대 재시도 횟수(3일 * 6회 = 18회) 체크
//            if (getRetryCount(subscription) >= 18) {
//                handleMaxRetriesExceeded(subscription);
//                continue;
//            }
//
//            try {
//                processSubscriptionRenewal(subscription);
//            } catch (Exception e) {
//                log.error("Retry payment failed for subscription: {}",
//                        subscription.getSubscriptionId(), e);
//                recordRetryAttempt(subscription);
//            }
//        }
//    }
//
//    // Helper methods
//    private void processSubscriptionRenewal(Subscription subscription) {
//        log.info("Processing renewal for subscription: {}", subscription.getSubscriptionId());
//        subscriptionService.renewSubscription(subscription.getSubscriptionId());
//        notificationService.sendRenewalSuccess(subscription);
//    }
//
//    private void handleRenewalFailure(Subscription subscription) {
//        subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
//        subscription.setUpdatedAt(LocalDateTime.now());
//        subscriptionRepository.save(subscription);
//        notificationService.sendRenewalFailure(subscription);
//    }
//
//    private void handleMaxRetriesExceeded(Subscription subscription) {
//        subscription.setStatus(SubscriptionStatus.EXPIRED);
//        subscription.setEndDate(LocalDateTime.now());
//        subscription.setUpdatedAt(LocalDateTime.now());
//        subscriptionRepository.save(subscription);
//        notificationService.sendSubscriptionExpired(subscription);
//    }
//
//    private int getRetryCount(Subscription subscription) {
//        // 재시도 횟수를 저장하고 조회하는 로직 구현
//        // 예: payment_logs 테이블에서 PAYMENT_FAILED 상태의 로그 수 조회
//        return 0; // TODO: Implement actual retry count logic
//    }
//
//    private void recordRetryAttempt(Subscription subscription) {
//        // 재시도 시도를 기록하는 로직 구현
//        // 예: payment_logs 테이블에 재시도 기록 저장
//    }
//}