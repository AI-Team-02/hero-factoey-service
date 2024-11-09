package ai.herofactoryservice.subscription.service;

import ai.herofactoryservice.subscription.entity.Subscription;
import ai.herofactoryservice.subscription.service.NCloudSmsService;
import ai.herofactoryservice.subscription.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EmailService emailService;
    private final NCloudSmsService smsService;

    public void sendRenewalReminder(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님의 구독이 3일 후인 %s에 갱신될 예정입니다. " +
                        "결제 예정 금액은 %d원입니다.",
                subscription.getMemberId(),
                subscription.getNextPaymentDate().toLocalDate(),
                subscription.getCurrentPrice()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "구독 갱신 예정 안내", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send renewal reminder for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }

    public void sendRenewalSuccess(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님의 구독이 성공적으로 갱신되었습니다. " +
                        "다음 결제일은 %s입니다.",
                subscription.getMemberId(),
                subscription.getNextPaymentDate().toLocalDate()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "구독 갱신 완료", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send renewal success notification for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }

    public void sendWelcomeMessage(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님, Hero Factory 구독을 시작해 주셔서 감사합니다. " +
                        "구독 기간은 %s부터 시작되며, 다음 결제일은 %s입니다. " +
                        "다양한 서비스를 마음껏 이용해 보세요!",
                subscription.getMemberId(),
                subscription.getStartDate().toLocalDate(),
                subscription.getNextPaymentDate().toLocalDate()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "Hero Factory 구독 시작 안내", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send welcome message for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }

    public void sendCancellationConfirmation(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님의 구독 취소가 정상적으로 처리되었습니다. " +
                        "현재 구독은 %s까지 이용하실 수 있습니다. " +
                        "그동안 Hero Factory를 이용해 주셔서 감사합니다.",
                subscription.getMemberId(),
                subscription.getEndDate().toLocalDate()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "구독 취소 확인", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send cancellation confirmation for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }

    public void sendPaymentFailure(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님의 구독 결제가 실패했습니다. " +
                        "결제 수단을 확인해 주시기 바랍니다. " +
                        "결제가 계속 실패할 경우 구독이 자동 해지될 수 있습니다.",
                subscription.getMemberId()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "구독 결제 실패", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send payment failure notification for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }

    public void sendPaymentRetrySuccess(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님의 구독 결제가 성공적으로 재시도되었습니다. " +
                        "다음 결제일은 %s입니다.",
                subscription.getMemberId(),
                subscription.getNextPaymentDate().toLocalDate()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "구독 결제 재시도 성공", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send payment retry success notification for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }

    public void sendSubscriptionExpired(Subscription subscription) {
        String message = String.format(
                "안녕하세요. %s님의 구독이 만료되었습니다. " +
                        "서비스 이용을 원하시면 재구독해 주시기 바랍니다.",
                subscription.getMemberId()
        );

        try {
            emailService.sendEmail(subscription.getMemberId(), "구독 만료", message);
            smsService.sendSms(subscription.getMemberId(), message);
        } catch (Exception e) {
            log.error("Failed to send subscription expired notification for subscription: {}",
                    subscription.getSubscriptionId(), e);
        }
    }
}