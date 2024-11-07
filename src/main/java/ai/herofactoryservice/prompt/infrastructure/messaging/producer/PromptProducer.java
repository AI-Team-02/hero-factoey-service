// PromptProducer.java
package ai.herofactoryservice.prompt.infrastructure.messaging.producer;

import ai.herofactoryservice.config.amqp.RabbitMQConfig;
import ai.herofactoryservice.common.exception.PromptException;
import ai.herofactoryservice.infrastructure.entity.MessageLog;
import ai.herofactoryservice.infrastructure.repository.MessageLogRepository;
import ai.herofactoryservice.prompt.dto.PromptMessage;
import ai.herofactoryservice.prompt.entity.Prompt;
import ai.herofactoryservice.prompt.entity.PromptLog;
import ai.herofactoryservice.prompt.repository.PromptLogRepository;
import ai.herofactoryservice.prompt.repository.PromptRepository;
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
    private final MessageLogRepository messageLogRepository;
    private final PromptLogRepository promptLogRepository;
    private final PromptRepository promptRepository;

    @Transactional
    public void sendPromptMessage(PromptMessage message) {
        String messageId = UUID.randomUUID().toString();

        try {
            // 메시징 인프라 로그
            saveMessageLog(message, messageId, "SENDING", null);

            // 비즈니스 로그
            Prompt prompt = promptRepository.findByPromptId(message.getPromptId())
                    .orElseThrow(() -> new PromptException("프롬프트를 찾을 수 없습니다."));
            savePromptLog(prompt, "MESSAGE_SENT", "메시지 큐로 전송 시작");

            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setTimestamp(Date.from(LocalDateTime.now()
                    .atZone(ZoneId.systemDefault()).toInstant()));

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

            saveMessageLog(message, messageId, "SENT", null);
            savePromptLog(prompt, "MESSAGE_SENT", "메시지 큐로 전송 완료");

            log.info("Prompt message sent successfully: {}", messageId);

        } catch (Exception e) {
            String errorMessage = "Failed to send prompt message: " + e.getMessage();
            saveMessageLog(message, messageId, "FAILED", errorMessage);

            Prompt prompt = promptRepository.findByPromptId(message.getPromptId()).orElse(null);
            if (prompt != null) {
                savePromptLog(prompt, "MESSAGE_FAILED", "메시지 전송 실패: " + errorMessage);
            }

            log.error("Failed to send prompt message: {}", messageId, e);
            throw new PromptException("메시지 전송 실패", e);
        }
    }

    private void saveMessageLog(PromptMessage message, String messageId, String status, String errorMessage) {
        try {
            MessageLog log = MessageLog.builder()
                    .messageId(messageId)
                    .promptId(message.getPromptId())
                    .paymentId(null)
                    .status(status)
                    .errorMessage(errorMessage)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            messageLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save message log: {}", messageId, e);
        }
    }

    private void savePromptLog(Prompt prompt, String logType, String content) {
        try {
            PromptLog log = PromptLog.builder()
                    .prompt(prompt)
                    .logType(logType)
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .build();
            promptLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save prompt log: {}", prompt.getPromptId(), e);
        }
    }

    @Transactional(readOnly = true)
    public boolean isMessageSent(String messageId) {
        return messageLogRepository.existsByMessageIdAndStatus(messageId, "SENT");
    }

    private void compressSketchData(PromptMessage message) {
        try {
            if (message.getSketchData() != null &&
                    message.getSketchData().length() > 1_000_000) {
                log.info("Compressing large sketch data for prompt: {}",
                        message.getPromptId());
                log.warn("Large sketch data detected. Size: {}",
                        message.getSketchData().length());
            }
        } catch (Exception e) {
            log.error("Failed to compress sketch data", e);
        }
    }
}