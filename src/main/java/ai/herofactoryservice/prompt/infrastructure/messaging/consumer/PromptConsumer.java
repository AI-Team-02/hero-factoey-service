package ai.herofactoryservice.prompt.infrastructure.messaging.consumer;

import ai.herofactoryservice.config.amqp.RabbitMQConfig;
import ai.herofactoryservice.infrastructure.entity.MessageLog;
import ai.herofactoryservice.infrastructure.repository.MessageLogRepository;
import ai.herofactoryservice.prompt.dto.PromptMessage;
import ai.herofactoryservice.prompt.entity.Prompt;
import ai.herofactoryservice.prompt.entity.PromptLog;
import ai.herofactoryservice.prompt.repository.PromptLogRepository;
import ai.herofactoryservice.prompt.repository.PromptRepository;
import ai.herofactoryservice.prompt.service.PromptService;
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

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptConsumer {
    private final PromptService promptService;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;
    private final MessageLogRepository messageLogRepository;
    private final PromptLogRepository promptLogRepository;
    private final PromptRepository promptRepository;

    @RabbitListener(queues = RabbitMQConfig.PROMPT_QUEUE)
    public void processPrompt(Message message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        String messageId = message.getMessageProperties().getMessageId();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            if (isMessageProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }

            PromptMessage promptMessage = extractPromptMessage(message);
            if (promptMessage == null) {
                rejectMessage(channel, tag, messageId, "Failed to parse message");
                return;
            }

            Prompt prompt = promptRepository.findByPromptId(promptMessage.getPromptId())
                    .orElseThrow(() -> new RuntimeException("프롬프트를 찾을 수 없습니다."));

            // 메시징 인프라 로그
            saveMessageLog(promptMessage, messageId, "PROCESSING", null);
            // 비즈니스 로그
            savePromptLog(prompt, "MESSAGE_PROCESSING", "메시지 처리 시작");

            promptService.processPrompt(promptMessage);

            saveMessageLog(promptMessage, messageId, "PROCESSED", null);
            savePromptLog(prompt, "MESSAGE_PROCESSED", "메시지 처리 완료");

            channel.basicAck(tag, false);
            transactionManager.commit(status);

        } catch (Exception e) {
            log.error("Error processing prompt message: {}", messageId, e);
            transactionManager.rollback(status);

            try {
                handleProcessingFailure(message, channel, tag, messageId, e);
            } catch (Exception ex) {
                log.error("Failed to handle message failure", ex);
                saveMessageLog(extractPromptMessage(message), messageId, "FAILED",
                        "Failed to handle message: " + ex.getMessage());

                try {
                    PromptMessage failedMessage = extractPromptMessage(message);
                    if (failedMessage != null) {
                        Prompt prompt = promptRepository.findByPromptId(failedMessage.getPromptId()).orElse(null);
                        if (prompt != null) {
                            savePromptLog(prompt, "MESSAGE_FAILED", "메시지 처리 실패: " + ex.getMessage());
                        }
                    }
                } catch (Exception logEx) {
                    log.error("Failed to save failure log", logEx);
                }
            }
        }
    }

    private void handleProcessingFailure(Message message, Channel channel,
                                         long tag, String messageId, Exception e) throws Exception {
        PromptMessage promptMessage = extractPromptMessage(message);
        saveMessageLog(promptMessage, messageId, "FAILED", e.getMessage());

        Prompt prompt = null;
        if (promptMessage != null) {
            prompt = promptRepository.findByPromptId(promptMessage.getPromptId()).orElse(null);
            if (prompt != null) {
                savePromptLog(prompt, "MESSAGE_FAILED", "메시지 처리 실패: " + e.getMessage());
            }
        }

        int failedCount = messageLogRepository.countFailedAttempts(messageId);

        if (failedCount >= 3) {
            log.warn("Message {} failed {} times, sending to DLQ", messageId, failedCount);
            if (prompt != null) {
                savePromptLog(prompt, "MESSAGE_FAILED",
                        String.format("최대 재시도 횟수(%d) 초과로 DLQ로 이동", failedCount));
            }
            channel.basicReject(tag, false);
        } else {
            log.warn("Message {} failed, attempt {}/3, requeueing", messageId, failedCount);
            if (prompt != null) {
                savePromptLog(prompt, "MESSAGE_REQUEUED",
                        String.format("메시지 재처리 예약 (시도: %d/3)", failedCount));
            }
            channel.basicNack(tag, false, true);
        }
    }

    private boolean isMessageProcessed(String messageId) {
        return messageLogRepository.existsByMessageIdAndStatus(messageId, "PROCESSED");
    }

    private PromptMessage extractPromptMessage(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), PromptMessage.class);
        } catch (Exception e) {
            log.error("Failed to extract prompt message", e);
            return null;
        }
    }

    private void saveMessageLog(PromptMessage message, String messageId, String status, String errorMessage) {
        try {
            MessageLog log = MessageLog.builder()
                    .messageId(messageId)
                    .promptId(message != null ? message.getPromptId() : null)
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

    private void rejectMessage(Channel channel, long tag, String messageId, String error) {
        try {
            channel.basicReject(tag, false);
            log.error("Rejected message {}: {}", messageId, error);
        } catch (Exception e) {
            log.error("Failed to reject message {}", messageId, e);
        }
    }
}