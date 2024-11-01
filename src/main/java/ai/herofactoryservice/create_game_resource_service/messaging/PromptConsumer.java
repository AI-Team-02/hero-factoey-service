package ai.herofactoryservice.create_game_resource_service.messaging;

import ai.herofactoryservice.create_game_resource_service.config.RabbitMQConfig;
import ai.herofactoryservice.create_game_resource_service.exception.PromptException;
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
                log.info("Message already processed: {}", messageId);
                channel.basicAck(tag, false);
                return;
            }

            String messageJson = new String(message.getBody());
            PromptMessage promptMessage = objectMapper.readValue(messageJson, PromptMessage.class);

            saveMessageLog(promptMessage, messageId, "PROCESSING", null);

            validateAndPreprocessSketchData(promptMessage);
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

    private void validateAndPreprocessSketchData(PromptMessage promptMessage) {
        if (promptMessage.getSketchData() != null) {
            if (!isValidBase64(promptMessage.getSketchData())) {
                throw new PromptException("Invalid sketch data format", "INVALID_SKETCH_DATA", "Base64 validation failed");
            }
            if (promptMessage.getSketchData().length() > 5_000_000) {
                throw new PromptException("Sketch data size exceeds limit", "SKETCH_SIZE_EXCEEDED", "Maximum size: 5MB");
            }
        }
    }

    private boolean isValidBase64(String base64Data) {
        return base64Data.matches("^[A-Za-z0-9+/]*={0,2}$");
    }

    private void handleProcessingFailure(Message message, Channel channel,
                                         long tag, String messageId, Exception e) {
        try {
            Integer retryCount = message.getMessageProperties().getXDeathHeader() != null ?
                    message.getMessageProperties().getXDeathHeader().size() : 0;

            saveMessageLog(extractPromptMessage(message), messageId, "FAILED", e.getMessage());

            if (retryCount >= 3) {
                log.warn("Message sent to DLQ after {} retries: {}", retryCount, messageId);
                channel.basicNack(tag, false, false);
            } else {
                log.warn("Message requeued for retry. Attempt {}: {}", retryCount + 1, messageId);
                channel.basicNack(tag, false, true);
            }
        } catch (Exception ex) {
            log.error("Error handling message processing failure", ex);
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
                    .build();
            messageLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save message log: {}", messageId, e);
        }
    }
}