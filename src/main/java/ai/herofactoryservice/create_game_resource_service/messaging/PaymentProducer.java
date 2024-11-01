package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.RabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.exception.PaymentException;
import ai.herofactoryservice.create_game_resource_service.model.MessageLog;
import ai.herofactoryservice.create_game_resource_service.model.PaymentMessage;
import ai.herofactoryservice.create_game_resource_service.repository.MessageLogRepository;
import ai.herofactoryservice.create_game_resource_service.repository.PaymentLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final MessageLogRepository messageLogRepository;

    @Transactional
    public void sendPaymentMessage(PaymentMessage message) {
        String messageId = UUID.randomUUID().toString();

        try {
            // 메시지 전송 전 로그 저장
            saveMessageLog(message, messageId, "SENDING", null);

            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            // LocalDateTime을 Date로 변환
            properties.setTimestamp(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

            String messageJson = objectMapper.writeValueAsString(message);
            Message amqpMessage = new Message(messageJson.getBytes(), properties);

            CorrelationData correlationData = new CorrelationData(messageId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_QUEUE,
                    amqpMessage,
                    correlationData
            );

            // 메시지 전송 성공 로그
            saveMessageLog(message, messageId, "SENT", null);
            log.info("Payment message sent successfully: {}", messageId);

        } catch (Exception e) {
            String errorMessage = "Failed to send payment message: " + e.getMessage();
            // 메시지 전송 실패 로그
            saveMessageLog(message, messageId, "FAILED", errorMessage);
            log.error("Failed to send payment message: {}", messageId, e);
            throw new PaymentException("메시지 전송 실패", e);
        }
    }

    private void saveMessageLog(PaymentMessage message, String messageId, String status, String errorMessage) {
        try {
            MessageLog log = MessageLog.builder()
                    .messageId(messageId)
                    .paymentId(message.getPaymentId())
                    .promptId(null)  // payment 메시지는 promptId가 없음
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            messageLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save message log: {}", messageId, e);
        }
    }

    @Transactional(readOnly = true)
    public boolean isMessageSent(String messageId) {
        return messageLogRepository.existsByMessageIdAndStatus(messageId, "SENT");
    }
}