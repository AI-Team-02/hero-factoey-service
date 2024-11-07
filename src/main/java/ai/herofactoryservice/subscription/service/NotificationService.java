//package ai.herofactoryservice.subscription.service;
//
//import ai.herofactoryservice.subscription.entity.Subscription;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class NotificationService {
//    private final EmailService emailService;
//    private final SmsService smsService;
//
//    public void sendRenewalReminder(Subscription subscription) {
//        String message = String.format(
//                "안녕하세요. %s님의 구독이 3일 후인 %s에 갱신될 예정입니다. " +
//                        "결제 예정 금액은 %d원입니다.",
//                subscription.getMemberId(),
//                subscription.getNextPaymentDate().toLocalDate(),
//                subscription.getAmount()
//        );
//
//        try {
//            emailService.sendEmail(subscription.getMemberId(), "구독 갱신 예정 안내", message);
//            smsService.sendSms(subscription.getMemberId(), message);
//        } catch (Exception e) {
//            log.error("Failed to send renewal reminder for subscription: {}",
//                    subscription.getSubscriptionId(), e);
//        }
//    }
//
//    public void sendRenewalSuccess(Subscription subscription) {
//        String message = String.format(
//                "안녕하세요. %s님의 구독이 성공적으로 갱신되었습니다. " +
//                        "다음 결제일은 %s입니다.",
//                subscription.getMemberId(),
//                subscription.getNextPaymentDate().toLocalDate()
//        );
//
//        try {
//            emailService.sendEmail(subscription.getMemberId(), "구독 갱신 완료", message);
//            smsService.sendSms(subscription.getMemberId(), message);
//        } catch (Exception e) {
//            log.error("Failed to send renewal success notification for subscription: {}",
//                    subscription.getSubscriptionId(), e);
//        }
//    }
//
//    public void sendRenewalFailure(Subscription subscription) {
//        String message = String.format(
//                "안녕하세요. %s님의 구독 갱신 과정에서 결제가 실패했습니다. " +
//                        "결제 수단을 확인해 주시기 바랍니다.",
//                subscription.getMemberId()
//        );
//
//        try {
//            emailService.sendEmail(subscription.getMemberId(), "구독 갱신 실패", message);
//            smsService.sendSms(subscription.getMemberId(), message);
//        } catch (Exception e) {
//            log.error("Failed to send renewal failure notification for subscription: {}",
//                    subscription.getSubscriptionId(), e);
//        }
//    }
//
//    public void sendSubscriptionExpired(Subscription subscription) {
//        String message = String.format(
//                "안녕하세요. %s님의 구독이 만료되었습니다. " +
//                        "서비스 이용을 원하시면 재구독해 주시기 바랍니다.",
//                subscription.getMemberId()
//        );
//
//        try {
//            emailService.sendEmail(subscription.getMemberId(), "구독 만료", message);
//            smsService.sendSms(subscription.getMemberId(), message);
//        } catch (Exception e) {
//            log.error("Failed to send subscription expired notification for subscription: {}",
//                    subscription.getSubscriptionId(), e);
//        }
//    }
//}
