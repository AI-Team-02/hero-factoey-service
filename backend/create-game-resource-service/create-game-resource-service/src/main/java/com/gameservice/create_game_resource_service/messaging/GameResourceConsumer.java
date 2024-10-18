package com.gameservice.create_game_resource_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.gameservice.create_game_resource_service.config.RabbitMQConfig;
import com.gameservice.create_game_resource_service.model.GameResourceCreationMessage;
import com.gameservice.create_game_resource_service.service.GameResourceService;
import com.gameservice.create_game_resource_service.service.FastAPIClientService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class GameResourceConsumer {

    private final GameResourceService gameResourceService;
    private final FastAPIClientService fastAPIClientService;
    private final ObjectMapper objectMapper;

    public GameResourceConsumer(GameResourceService gameResourceService, FastAPIClientService fastAPIClientService, ObjectMapper objectMapper) {
        this.gameResourceService = gameResourceService;
        this.fastAPIClientService = fastAPIClientService;
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processGameResourceCreation(String messageJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            log.debug("Received message: {}", messageJson);

            // 이중 JSON 인코딩 처리
            if (messageJson.startsWith("\"") && messageJson.endsWith("\"")) {
                messageJson = messageJson.substring(1, messageJson.length() - 1);
            }
            messageJson = messageJson.replace("\\\"", "\"");

            GameResourceCreationMessage message = objectMapper.readValue(messageJson, GameResourceCreationMessage.class);
            log.info("Parsed message for task: {}. File name: {}", message.getTaskId(), message.getFileName());

            processGameResourceCreationMessage(message);

            channel.basicAck(tag, false);
            log.info("Message processed successfully for task: {}", message.getTaskId());
        } catch (Exception e) {
            log.error("Error processing message: " + messageJson, e);
            try {
                channel.basicNack(tag, false, false);
                log.warn("Message sent to DLQ: {}", tag);
            } catch (IOException ioException) {
                log.error("Error sending nack", ioException);
            }
        }
    }

    private void processGameResourceCreationMessage(GameResourceCreationMessage message) {
        try {
            MultipartFile file = gameResourceService.convertToMultipartFile(message);
            Map<String, Object> result = fastAPIClientService.generateImage(file, message.getRequest().getPrompt());

            String taskId = message.getTaskId();
            gameResourceService.processTaskUpdate(taskId, result);

            while (true) {
                Map<String, Object> status = fastAPIClientService.getTaskStatus((String) result.get("task_id"));
                gameResourceService.processTaskUpdate(taskId, status);

                if ("completed".equals(status.get("status")) || "failed".equals(status.get("status"))) {
                    break;
                }

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.error("Error processing game resource creation message", e);
        }
    }
}