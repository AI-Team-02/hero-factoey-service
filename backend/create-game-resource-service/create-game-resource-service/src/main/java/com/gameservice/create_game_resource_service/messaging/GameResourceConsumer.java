package com.gameservice.create_game_resource_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameservice.create_game_resource_service.config.RabbitMQConfig;
import com.gameservice.create_game_resource_service.model.GameResourceCreationMessage;
import com.gameservice.create_game_resource_service.service.FastAPIClientService;
import com.gameservice.create_game_resource_service.service.GameResourceService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class GameResourceConsumer {

    private final GameResourceService gameResourceService;
    private final FastAPIClientService fastAPIClientService;
    private final ObjectMapper objectMapper;

    @Autowired
    public GameResourceConsumer(GameResourceService gameResourceService, FastAPIClientService fastAPIClientService, ObjectMapper objectMapper) {
        this.gameResourceService = gameResourceService;
        this.fastAPIClientService = fastAPIClientService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processGameResourceCreation(String messageJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            log.debug("Received message: {}", messageJson);

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
            MultipartFile file = convertToMultipartFile(message);
            Map<String, Object> result = fastAPIClientService.generateImage(file, message.getRequest().getPrompt());

            String taskId = message.getTaskId();
            gameResourceService.updateTaskStatus(taskId, result);

            String generationTaskId = (String) result.get("task_id");
            while (true) {
                Map<String, Object> status = fastAPIClientService.getTaskStatus(generationTaskId);
                gameResourceService.updateTaskStatus(taskId, status);

                if ("completed".equals(status.get("status")) || "failed".equals(status.get("status"))) {
                    break;
                }

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.error("Error processing game resource creation message", e);
        }
    }
    public MultipartFile convertToMultipartFile(GameResourceCreationMessage message) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return message.getFileName();
            }

            @Override
            public String getOriginalFilename() {
                return message.getFileName();
            }

            @Override
            public String getContentType() {
                return "image/png";  // 또는 적절한 콘텐츠 타입
            }

            @Override
            public boolean isEmpty() {
                return message.getFileContent() == null || message.getFileContent().isEmpty();
            }

            @Override
            public long getSize() {
                return message.getFileContent().length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Base64.getDecoder().decode(message.getFileContent());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(Base64.getDecoder().decode(message.getFileContent()));
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                throw new UnsupportedOperationException("transferTo is not supported");
            }
        };
    }
}