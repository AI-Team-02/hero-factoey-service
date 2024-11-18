package com.herofactory.subscription.scheduler;

import com.herofactory.payment.entity.PaymentLog;
import com.herofactory.payment.repository.PaymentLogRepository;
import com.herofactory.subscription.entity.Subscription;
import com.herofactory.subscription.entity.enums.SubscriptionStatus;
import com.herofactory.subscription.repository.SubscriptionRepository;
import com.herofactory.subscription.service.NotificationService;
import com.herofactory.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;
    private final PaymentLogRepository paymentLogRepository;

    private static final int MAX_RETRY_ATTEMPTS = 18; // 3일 * 6회 = 18회
    private static final String RETRY_LOG_TYPE = "PAYMENT_RETRY";
    private static final String FAILURE_LOG_TYPE = "PAYMENT_FAILURE";

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processSubscriptionRenewals() {
        log.info("Starting daily subscription renewal processing");
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> dueSubscriptions = subscriptionRepository.findDueSubscriptions(now);
        log.info("Found {} subscriptions due for renewal", dueSubscriptions.size());

        for (Subscription subscription : dueSubscriptions) {
            try {
                processSubscriptionRenewal(subscription);
                log.info("Successfully processed renewal for subscription: {}",
                        subscription.getSubscriptionId());
            } catch (Exception e) {
                log.error("Failed to process renewal for subscription: {} - {}",
                        subscription.getSubscriptionId(), e.getMessage());
                handleRenewalFailure(subscription, e);
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendRenewalReminders() {
        log.info("Starting renewal reminder processing");
        LocalDateTime startDate = LocalDateTime.now().plusDays(3);
        LocalDateTime endDate = startDate.plusDays(1);

        List<Subscription> upcomingRenewals =
                subscriptionRepository.findUpcomingRenewals(startDate, endDate);
        log.info("Found {} subscriptions for renewal reminders", upcomingRenewals.size());

        for (Subscription subscription : upcomingRenewals) {
            try {
                notificationService.sendRenewalReminder(subscription);
                log.info("Sent renewal reminder for subscription: {}",
                        subscription.getSubscriptionId());
            } catch (Exception e) {
                log.error("Failed to send renewal reminder for subscription: {} - {}",
                        subscription.getSubscriptionId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 4, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void retryFailedPayments() {
        log.info("Starting failed payment retry processing");
        List<Subscription> failedSubscriptions =
                subscriptionRepository.findByStatus(SubscriptionStatus.PAYMENT_FAILED);
        log.info("Found {} failed subscriptions to retry", failedSubscriptions.size());

        for (Subscription subscription : failedSubscriptions) {
            String subscriptionId = subscription.getSubscriptionId();
            Long paymentId = subscription.getLastPayment().getPayment().getId();
            long retryCount = paymentLogRepository.countByPaymentIdAndLogType(paymentId, RETRY_LOG_TYPE);

            if (retryCount >= MAX_RETRY_ATTEMPTS) {
                log.warn("Max retry attempts exceeded for subscription: {}", subscriptionId);
                handleMaxRetriesExceeded(subscription);
                continue;
            }

            try {
                processSubscriptionRenewal(subscription);
                recordPaymentLog(subscription, RETRY_LOG_TYPE, "Payment retry successful");
                notificationService.sendPaymentRetrySuccess(subscription);
                log.info("Successfully retried payment for subscription: {}", subscriptionId);
            } catch (Exception e) {
                log.error("Retry payment failed for subscription: {} - {}",
                        subscriptionId, e.getMessage());
                recordPaymentLog(subscription, RETRY_LOG_TYPE,
                        "Payment retry failed: " + e.getMessage());
                notificationService.sendPaymentFailure(subscription);
            }
        }
    }

    private void processSubscriptionRenewal(Subscription subscription) {
        log.debug("Processing renewal for subscription: {}", subscription.getSubscriptionId());
        subscriptionService.renewSubscription(subscription.getSubscriptionId());
        notificationService.sendRenewalSuccess(subscription);
    }

    private void handleRenewalFailure(Subscription subscription, Exception e) {
        subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        recordPaymentLog(subscription, FAILURE_LOG_TYPE,
                "Payment failed: " + e.getMessage());
        notificationService.sendPaymentFailure(subscription);
    }

    private void handleMaxRetriesExceeded(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setEndDate(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        recordPaymentLog(subscription, FAILURE_LOG_TYPE,
                "Subscription expired due to maximum retry attempts");
        notificationService.sendSubscriptionExpired(subscription);
    }

    private void recordPaymentLog(Subscription subscription, String logType, String content) {
        PaymentLog log = PaymentLog.builder()
                .payment(subscription.getLastPayment().getPayment())
                .logType(logType)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        paymentLogRepository.save(log);
    }
}