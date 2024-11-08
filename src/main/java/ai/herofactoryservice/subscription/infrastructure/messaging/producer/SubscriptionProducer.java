package ai.herofactoryservice.subscription.infrastructure.messaging.producer;

import ai.herofactoryservice.subscription.entity.Subscription;
import ai.herofactoryservice.subscription.infrastructure.messaging.SubscriptionMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public static final String SUBSCRIPTION_QUEUE = "subscription-queue";
    public static final String SUBSCRIPTION_EXCHANGE = "subscription-exchange";

    public void sendSubscriptionMessage(Subscription subscription, String eventType) {
        String messageId = UUID.randomUUID().toString();

        try {
            SubscriptionMessage message = createSubscriptionMessage(subscription, eventType);
            MessageProperties properties = createMessageProperties(messageId);
            String messageJson = objectMapper.writeValueAsString(message);

            Message amqpMessage = new Message(messageJson.getBytes(), properties);

            rabbitTemplate.convertAndSend(
                    SUBSCRIPTION_EXCHANGE,
                    SUBSCRIPTION_QUEUE,
                    amqpMessage
            );

            log.info("Subscription message sent - Type: {}, ID: {}", eventType, messageId);

        } catch (Exception e) {
            log.error("Failed to send subscription message: {}", messageId, e);
            throw new RuntimeException("메시지 전송 실패", e);
        }
    }

    private SubscriptionMessage createSubscriptionMessage(Subscription subscription, String eventType) {
        return SubscriptionMessage.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .memberId(subscription.getMemberId())
                .eventType(eventType)
                .planId(subscription.getPlan().getId())
                .billingCycle(subscription.getBillingCycle().name())
                .amount(subscription.getCurrentPrice())
                .build();
    }

    private MessageProperties createMessageProperties(String messageId) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(messageId);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setTimestamp(Date.from(LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant()));
        return properties;
    }
}