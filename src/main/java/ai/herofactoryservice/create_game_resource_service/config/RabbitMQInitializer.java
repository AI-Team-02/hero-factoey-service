//package ai.herofactoryservice.create_game_resource_service.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PostConstruct;
//
//@Component
//@Slf4j
//public class RabbitMQInitializer {
//    private final AmqpAdmin amqpAdmin;
//
//    @Autowired
//    public RabbitMQInitializer(AmqpAdmin amqpAdmin) {
//        this.amqpAdmin = amqpAdmin;
//    }
//
//    @PostConstruct
//    public void initialize() {
//        try {
//            log.info("Starting RabbitMQ initialization...");
//
//            log.info("Declaring exchange: {}", RabbitMQConfig.EXCHANGE_NAME);
//            Exchange exchange = new DirectExchange(RabbitMQConfig.EXCHANGE_NAME);
//            amqpAdmin.declareExchange(exchange);
//
//            log.info("Declaring queue: {}", RabbitMQConfig.QUEUE_NAME);
//            Queue queue = new Queue(RabbitMQConfig.QUEUE_NAME, true, false, false);
//            amqpAdmin.declareQueue(queue);
//
//            log.info("Declaring binding between {} and {}", RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.QUEUE_NAME);
//            Binding binding = new Binding(RabbitMQConfig.QUEUE_NAME, Binding.DestinationType.QUEUE,
//                    RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.QUEUE_NAME, null);
//            amqpAdmin.declareBinding(binding);
//
//            log.info("RabbitMQ initialization completed successfully");
//        } catch (Exception e) {
//            log.error("Failed to initialize RabbitMQ", e);
//            throw new RuntimeException("Failed to initialize RabbitMQ", e);
//        }
//    }
//}
