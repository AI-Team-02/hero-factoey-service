package ai.herofactoryservice.prompt.infrastructure.messaging.producer;

import ai.herofactoryservice.config.amqp.RabbitMQConfig;
import ai.herofactoryservice.common.exception.PromptException;
import ai.herofactoryservice.prompt.entity.PromptLog;
import ai.herofactoryservice.prompt.dto.messaging.PromptMessage;
import ai.herofactoryservice.prompt.repository.PromptLogRepository;
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
public class PromptProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final PromptLogRepository promptLogRepository;

    @Transactional
    public void sendPromptMessage(PromptMessage message) {
        String messageId = UUID.randomUUID().toString();

        try {
            // 메시지 전송 전 로그 저장
            saveMessageLog(message, messageId, "SENDING", null);

            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setTimestamp(Date.from(LocalDateTime.now()
                    .atZone(ZoneId.systemDefault()).toInstant()));

            // 스케치 데이터가 있는 경우 압축 처리
            if (message.getSketchData() != null) {
                compressSketchData(message);
            }

            String messageJson = objectMapper.writeValueAsString(message);
            Message amqpMessage = new Message(messageJson.getBytes(), properties);

            CorrelationData correlationData = new CorrelationData(messageId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PROMPT_EXCHANGE,
                    RabbitMQConfig.PROMPT_QUEUE,
                    amqpMessage,
                    correlationData
            );

            // 메시지 전송 성공 로그
            saveMessageLog(message, messageId, "SENT", null);
            log.info("Prompt message sent successfully: {}", messageId);

        } catch (Exception e) {
            String errorMessage = "Failed to send prompt message: " + e.getMessage();
            // 메시지 전송 실패 로그
            saveMessageLog(message, messageId, "FAILED", errorMessage);
            log.error("Failed to send prompt message: {}", messageId, e);
            throw new PromptException("메시지 전송 실패", e);
        }
    }

    private void saveMessageLog(PromptMessage message, String messageId, String status, String errorMessage) {
        try {
            PromptLog log = PromptLog.builder()
                    .messageId(messageId)
                    .promptId(message.getPromptId())
                    .paymentId(null)  // prompt 메시지는 paymentId가 없음
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            promptLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save message log: {}", messageId, e);
        }
    }

    @Transactional(readOnly = true)
    public boolean isMessageSent(String messageId) {
        return promptLogRepository.existsByMessageIdAndStatus(messageId, "SENT");
    }

    private void compressSketchData(PromptMessage message) {
        try {
            // Base64로 인코딩된 스케치 데이터가 너무 큰 경우를 대비한 압축 처리
            if (message.getSketchData() != null &&
                    message.getSketchData().length() > 1_000_000) { // 1MB 초과

                log.info("Compressing large sketch data for prompt: {}",
                        message.getPromptId());

                // 이미지 품질 조정 또는 크기 축소 로직 구현
                // 예: Base64 디코딩 → 이미지 압축 → Base64 인코딩
                // 현재는 로깅만 수행
                log.warn("Large sketch data detected. Size: {}",
                        message.getSketchData().length());
            }
        } catch (Exception e) {
            log.error("Failed to compress sketch data", e);
            // 압축 실패 시 원본 데이터 유지
        }
    }
}