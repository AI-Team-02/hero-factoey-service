package ai.herofactoryservice.subscription.infrastructure.messaging.consumer;

import ai.herofactoryservice.subscription.entity.Subscription;
import ai.herofactoryservice.subscription.infrastructure.messaging.SubscriptionMessage;
import ai.herofactoryservice.subscription.service.MetricsService;
//import ai.herofactoryservice.subscription.service.NotificationService;
import ai.herofactoryservice.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionConsumer {
    private final SubscriptionService subscriptionService;
//    private final NotificationService notificationService;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

//    @RabbitListener(queues = "${subscription.queue.name}")
//    public void processSubscriptionMessage(Message message, Channel channel,
//                                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
//        String messageId = message.getMessageProperties().getMessageId();
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
//        TransactionStatus status = transactionManager.getTransaction(def);
//
//        try {
//            SubscriptionMessage subscriptionMessage = objectMapper.readValue(
//                    new String(message.getBody()),
//                    SubscriptionMessage.class
//            );
//
//            Subscription subscription = subscriptionService
//                    .getSubscription(subscriptionMessage.getSubscriptionId());
//
//            switch (subscriptionMessage.getEventType()) {
//                case "CREATED" -> handleSubscriptionCreated(subscription);
//                case "RENEWED" -> handleSubscriptionRenewed(subscription);
//                case "CANCELLED" -> handleSubscriptionCancelled(subscription);
//                case "EXPIRED" -> handleSubscriptionExpired(subscription);
//                case "PAYMENT_FAILED" -> handlePaymentFailed(subscription);
//            }
//
//            // 메트릭스 업데이트
//            metricsService.recordSubscriptionEvent(subscription,
//                    subscriptionMessage.getEventType());
//
//            channel.basicAck(tag, false);
//            transactionManager.commit(status);
//
//        } catch (Exception e) {
//            transactionManager.rollback(status);
//            handleProcessingError(message, channel, tag, messageId, e);
//        }
//    }

//    private void handleSubscriptionCreated(Subscription subscription) {
//        notificationService.sendWelcomeMessage(subscription);
//    }

//    private void handleSubscriptionRenewed(Subscription subscription) {
//        notificationService.sendRenewalSuccess(subscription);
//    }

//    private void handleSubscriptionCancelled(Subscription subscription) {
//        notificationService.sendCancellationConfirmation(subscription);
//    }

//    private void handleSubscriptionExpired(Subscription subscription) {
//        notificationService.sendSubscriptionExpired(subscription);
//    }

//    private void handlePaymentFailed(Subscription subscription) {
//        notificationService.sendPaymentFailure(subscription);
//    }

    private void handleProcessingError(Message message, Channel channel,
                                       long tag, String messageId, Exception e) {
        try {
            log.error("Error processing subscription message: {}", messageId, e);

            int retryCount = getRetryCount(message);
            if (retryCount >= 3) {
                log.warn("Message {} failed {} times, sending to DLQ", messageId, retryCount);
                channel.basicReject(tag, false);
            } else {
                log.warn("Message {} failed, attempt {}/3, requeueing", messageId, retryCount);
                channel.basicNack(tag, false, true);
                message.getMessageProperties().getHeaders().put("retry-count", retryCount + 1);
            }
        } catch (Exception ex) {
            log.error("Error handling message failure", ex);
        }
    }

    private int getRetryCount(Message message) {
        return (Integer) message.getMessageProperties()
                .getHeaders()
                .getOrDefault("retry-count", 0);
    }
}