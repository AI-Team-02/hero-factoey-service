package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.RabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.model.PaymentMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentMessage(PaymentMessage message) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            String messageJson = objectMapper.writeValueAsString(message);
            Message amqpMessage = new Message(messageJson.getBytes(), properties);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_QUEUE,
                    amqpMessage
            );
            log.info("Payment message sent: {}", message.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to send payment message: {}", message.getPaymentId(), e);
            throw new RuntimeException("메시지 전송 실패", e);
        }
    }
}