package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.RabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.model.PaymentMessage;
import ai.herofactoryservice.create_game_resource_service.service.KakaoPayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final KakaoPayService kakaoPayService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void processPayment(Message message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            // 메시지 처리 시작 로깅
            String messageJson = new String(message.getBody());
            PaymentMessage paymentMessage = objectMapper.readValue(messageJson, PaymentMessage.class);
            log.info("Payment message received: {}", paymentMessage.getPaymentId());

            // 재시도 횟수 확인
            Integer retryCount = message.getMessageProperties().getXDeathHeader() != null ?
                    message.getMessageProperties().getXDeathHeader().size() : 0;

            try {
                kakaoPayService.processPayment(paymentMessage);

                // 성공적인 처리 확인
                channel.basicAck(tag, false);
                log.info("Payment processed successfully: {}", paymentMessage.getPaymentId());
            } catch (Exception e) {
                // 비즈니스 로직 처리 실패
                handleProcessingFailure(channel, tag, retryCount, paymentMessage, e);
            }
        } catch (Exception e) {
            // 메시지 파싱 실패 등 기타 오류
            log.error("Critical error processing message", e);
            try {
                // 심각한 오류는 바로 DLQ로
                channel.basicNack(tag, false, false);
                log.warn("Message sent to DLQ due to critical error");
            } catch (Exception ex) {
                log.error("Error sending nack", ex);
            }
        }
    }

    private void handleProcessingFailure(Channel channel, long tag,
                                         Integer retryCount, PaymentMessage message,
                                         Exception e) throws IOException {
        log.error("Error processing payment: {}", message.getPaymentId(), e);

        if (retryCount >= 3) {
            // 최대 재시도 횟수 초과 - DLQ로 전송
            channel.basicNack(tag, false, false);
            log.warn("Payment message sent to DLQ after {} retries. PaymentId: {}",
                    retryCount, message.getPaymentId());
        } else {
            // 재시도를 위해 다시 큐로
            channel.basicNack(tag, false, true);
            log.warn("Payment message requeued for retry. Attempt: {}. PaymentId: {}",
                    retryCount + 1, message.getPaymentId());
        }
    }
}