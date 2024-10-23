package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.PaymentRabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.model.PaymentMessage;
import ai.herofactoryservice.create_game_resource_service.service.KakaoPayService;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final KakaoPayService kakaoPayService;  // 이름 변경
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = PaymentRabbitMQConfig.PAYMENT_QUEUE)
    public void processPayment(String messageJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            PaymentMessage message = objectMapper.readValue(messageJson, PaymentMessage.class);
            log.info("Payment message received: {}", message.getPaymentId());

            kakaoPayService.processPayment(message);  // 서비스 이름 변경

            channel.basicAck(tag, false);
            log.info("Payment processed successfully: {}", message.getPaymentId());
        } catch (Exception e) {
            log.error("Error processing payment message", e);
            try {
                channel.basicNack(tag, false, false);
                log.warn("Payment message sent to DLQ: {}", tag);
            } catch (Exception ex) {
                log.error("Error sending nack", ex);
            }
        }
    }
}