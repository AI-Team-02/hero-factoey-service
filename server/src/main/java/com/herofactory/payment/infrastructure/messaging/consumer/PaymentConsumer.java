package com.herofactory.payment.infrastructure.messaging.consumer;

import com.herofactory.config.amqp.RabbitMQConfig;
import com.herofactory.infrastructure.entity.MessageLog;
import com.herofactory.infrastructure.repository.MessageLogRepository;
import com.herofactory.payment.dto.PaymentMessage;
import com.herofactory.payment.service.KakaoPayService;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final KakaoPayService kakaoPayService;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;
    private final MessageLogRepository messageLogRepository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void processPayment(Message message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        String messageId = message.getMessageProperties().getMessageId();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // 메시지 처리 시작 로그
            String messageJson = new String(message.getBody());
            PaymentMessage paymentMessage = objectMapper.readValue(messageJson, PaymentMessage.class);

            // 멱등성 체크
            if (isMessageProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }

            saveMessageLog(paymentMessage, messageId, "PROCESSING", null);

            // 결제 처리
            kakaoPayService.processPayment(paymentMessage);

            // 메시지 처리 완료 표시
            saveMessageLog(paymentMessage, messageId, "PROCESSED", null);

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
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            Integer retryCount = (Integer) headers.getOrDefault("retry-count", 0);

            String errorMessage = "Payment processing failed: " + e.getMessage();
            saveMessageLog(extractPaymentMessage(message), messageId, "FAILED", errorMessage);

            if (retryCount >= 3) {
                channel.basicNack(tag, false, false);
                log.warn("Message sent to DLQ after {} retries. MessageId: {}",
                        retryCount, messageId);
            } else {
                headers.put("retry-count", retryCount + 1);
                channel.basicNack(tag, false, true);
                log.warn("Message requeued for retry. Attempt: {}. MessageId: {}",
                        retryCount + 1, messageId);
            }
        } catch (Exception ex) {
            log.error("Error handling message processing failure", ex);
            try {
                channel.basicReject(tag, false);
            } catch (IOException ioException) {
                log.error("Failed to reject message", ioException);
            }
        }
    }

    private PaymentMessage extractPaymentMessage(Message message) {
        try {
            return objectMapper.readValue(new String(message.getBody()), PaymentMessage.class);
        } catch (Exception e) {
            log.error("Failed to extract payment message", e);
            return null;
        }
    }

    private void saveMessageLog(PaymentMessage message, String messageId,
                                String status, String errorMessage) {
        try {
            MessageLog log = MessageLog.builder()
                    .messageId(messageId)
                    .paymentId(message != null ? message.getPaymentId() : null)
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