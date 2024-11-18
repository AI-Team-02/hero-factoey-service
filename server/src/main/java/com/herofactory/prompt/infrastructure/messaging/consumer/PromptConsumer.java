package com.herofactory.prompt.infrastructure.messaging.consumer;

import com.herofactory.config.amqp.RabbitMQConfig;
import com.herofactory.infrastructure.entity.MessageLog;
import com.herofactory.infrastructure.repository.MessageLogRepository;
import com.herofactory.prompt.dto.PromptMessage;
import com.herofactory.prompt.entity.Prompt;
import com.herofactory.prompt.entity.PromptLog;
import com.herofactory.prompt.repository.PromptLogRepository;
import com.herofactory.prompt.repository.PromptRepository;
import com.herofactory.prompt.service.PromptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptConsumer {
    private final PromptService promptService;
    private final ObjectMapper objectMapper;
    private final MessageLogRepository messageLogRepository;
    private final PromptLogRepository promptLogRepository;
    private final PromptRepository promptRepository;

    @RabbitListener(queues = RabbitMQConfig.PROMPT_QUEUE)
    public void processPrompt(Message message, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        String messageId = message.getMessageProperties().getMessageId();

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

            // 로깅
            saveMessageLog(promptMessage, messageId, "PROCESSING", null);
            savePromptLog(prompt, "MESSAGE_PROCESSING", "메시지 처리 시작");

            // 프롬프트 처리 - 서비스 내부에서 트랜잭션 관리
            promptService.processPrompt(promptMessage);

            // 성공 로깅
            saveMessageLog(promptMessage, messageId, "PROCESSED", null);
            savePromptLog(prompt, "MESSAGE_PROCESSED", "메시지 처리 완료");

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Error processing prompt message: {}", messageId, e);
            handleProcessingFailure(message, channel, tag, messageId, e);
        }
    }

    private void handleProcessingFailure(Message message, Channel channel,
                                         long tag, String messageId, Exception e) {
        try {
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
        } catch (Exception ex) {
            log.error("Error handling message processing failure", ex);
            try {
                channel.basicReject(tag, false);
            } catch (Exception ioException) {
                log.error("Failed to reject message", ioException);
            }
        }
    }

    @Transactional(readOnly = true)
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

    @Transactional
    protected void saveMessageLog(PromptMessage message, String messageId, String status, String errorMessage) {
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

    @Transactional
    protected void savePromptLog(Prompt prompt, String logType, String content) {
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