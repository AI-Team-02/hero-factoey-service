package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.RabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.model.MessageLog;
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
            if (isMessageProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }

            String messageJson = new String(message.getBody());
            PromptMessage promptMessage = objectMapper.readValue(messageJson, PromptMessage.class);

            saveMessageLog(promptMessage, messageId, "PROCESSING", null);
            promptService.processPrompt(promptMessage);
            saveMessageLog(promptMessage, messageId, "PROCESSED", null);

            channel.basicAck(tag, false);
            transactionManager.commit(status);

        } catch (Exception e) {
            transactionManager.rollback(status);
            handleProcessingFailure(message, channel, tag, messageId, e);
        }
    }

    private boolean isMessageProcessed(String messageId) {
        return messageLogRepository.existsByMessageIdAndStatus(messageId, "PROCESSED");
    }

    private void handleProcessingFailure(Message message, Channel channel,
                                         long tag, String messageId, Exception e) {
        try {
            saveMessageLog(extractPromptMessage(message), messageId, "FAILED", e.getMessage());

            int failedCount = messageLogRepository.countFailedAttempts(messageId);

            if (failedCount >= 3) {
                log.warn("Message sent to DLQ after {} failures: {}", failedCount, messageId);
                channel.basicReject(tag, false);
            } else {
                log.warn("Message requeued for retry. Failure count {}: {}", failedCount + 1, messageId);
                channel.basicNack(tag, false, true);
            }
        } catch (Exception ex) {
            log.error("Error handling message processing failure", ex);
            try {
                channel.basicReject(tag, false);
            } catch (Exception ioException) {
                log.error("Failed to reject message", ioException);
            }
        }
    }

    private PromptMessage extractPromptMessage(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), PromptMessage.class);
        } catch (Exception e) {
            log.error("Failed to extract prompt message", e);
            return null;
        }
    }

    private void saveMessageLog(PromptMessage message, String messageId,
                                String status, String errorMessage) {
        try {
            MessageLog log = MessageLog.builder()
                    .messageId(messageId)
                    .promptId(message != null ? message.getPromptId() : null)
                    .status(status)
                    .errorMessage(errorMessage)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save message log: {}", messageId, e);
        }
    }
}