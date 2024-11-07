package ai.herofactoryservice.prompt.infrastructure.messaging.consumer;

import ai.herofactoryservice.config.amqp.RabbitMQConfig;
import ai.herofactoryservice.prompt.MessageLog;
import ai.herofactoryservice.create_game_resource_service.model.PromptMessage;
import ai.herofactoryservice.create_game_resource_service.repository.MessageLogRepository;
import ai.herofactoryservice.create_game_resource_service.service.PromptService;
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

    @RabbitListener(queues = RabbitMQConfig.PROMPT_QUEUE)
    public void processPrompt(Message message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        String messageId = message.getMessageProperties().getMessageId();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // 이미 처리된 메시지 체크
            if (isMessageProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }

            PromptMessage promptMessage = extractPromptMessage(message);
            if (promptMessage == null) {
                // 메시지 파싱 실패시 DLQ로 전송
                rejectMessage(channel, tag, messageId, "Failed to parse message");
                return;
            }

            // 메시지 처리 시작 로그
            saveMessageLog(promptMessage, messageId, "PROCESSING", null);

            // 프롬프트 처리
            promptService.processPrompt(promptMessage);

            // 성공 처리
            saveMessageLog(promptMessage, messageId, "PROCESSED", null);
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
            }
        }
    }

    private void handleProcessingFailure(Message message, Channel channel,
                                         long tag, String messageId, Exception e) throws Exception {
        PromptMessage promptMessage = extractPromptMessage(message);
        saveMessageLog(promptMessage, messageId, "FAILED", e.getMessage());

        int failedCount = messageLogRepository.countFailedAttempts(messageId);

        if (failedCount >= 3) {
            log.warn("Message {} failed {} times, sending to DLQ", messageId, failedCount);
            channel.basicReject(tag, false);
        } else {
            log.warn("Message {} failed, attempt {}/3, requeueing", messageId, failedCount);
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

    private void rejectMessage(Channel channel, long tag, String messageId, String error) {
        try {
            channel.basicReject(tag, false);
            log.error("Rejected message {}: {}", messageId, error);
        } catch (Exception e) {
            log.error("Failed to reject message {}", messageId, e);
        }
    }
}