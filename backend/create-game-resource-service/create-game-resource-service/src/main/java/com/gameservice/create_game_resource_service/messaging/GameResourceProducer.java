package com.gameservice.create_game_resource_service.messaging;

import com.gameservice.create_game_resource_service.config.RabbitMQConfig;
import com.gameservice.create_game_resource_service.model.GameResourceCreationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameResourceProducer {

    private final RabbitTemplate rabbitTemplate;

    public GameResourceProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.QUEUE_NAME, message);
            log.info("Message sent successfully to exchange: {}", message);
        } catch (AmqpException e) {
            log.error("Failed to send message: {}", message, e);
        }
    }

    public void sendGameResourceCreationMessage(GameResourceCreationMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
            log.info("게임 리소스 생성 메시지를 큐에 전송: {}", message);
        } catch (AmqpException e) {
            log.error("게임 리소스 생성 메시지를 큐에 전송 실패", e);
            // 여기에 재시도 메커니즘 구현 또는 사용자 정의 예외 throw 가능
        }
    }
}