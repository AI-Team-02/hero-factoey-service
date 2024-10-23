package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.PaymentRabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.model.PaymentMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentMessage(PaymentMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(
                    PaymentRabbitMQConfig.PAYMENT_EXCHANGE,
                    PaymentRabbitMQConfig.PAYMENT_QUEUE,
                    messageJson
            );
            log.info("Payment message sent: {}", message.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to send payment message: {}", message.getPaymentId(), e);
            throw new RuntimeException("메시지 전송 실패", e);
        }
    }
}
