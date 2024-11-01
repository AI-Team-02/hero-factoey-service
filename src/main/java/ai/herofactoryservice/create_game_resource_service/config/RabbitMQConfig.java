package ai.herofactoryservice.create_game_resource_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String PAYMENT_QUEUE = "payment-queue";
    public static final String PAYMENT_EXCHANGE = "payment-exchange";
    public static final String PAYMENT_DLQ = "payment-dlq";
    public static final String PAYMENT_DLX = "payment-dlx";
    public static final String PROMPT_QUEUE = "prompt-queue";
    public static final String PROMPT_EXCHANGE = "prompt-exchange";
    public static final String PROMPT_DLQ = "prompt-dlq";
    public static final String PROMPT_DLX = "prompt-dlx";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Payment Queue Configuration
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_DLQ)
                .build();
    }

    @Bean
    public Queue paymentDeadLetterQueue() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange paymentDeadLetterExchange() {
        return new DirectExchange(PAYMENT_DLX);
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentExchange())
                .with(PAYMENT_QUEUE);
    }

    @Bean
    public Binding paymentDeadLetterBinding() {
        return BindingBuilder.bind(paymentDeadLetterQueue())
                .to(paymentDeadLetterExchange())
                .with(PAYMENT_DLQ);
    }

    // Prompt Queue Configuration
    @Bean
    public Queue promptQueue() {
        return QueueBuilder.durable(PROMPT_QUEUE)
                .withArgument("x-dead-letter-exchange", PROMPT_DLX)
                .withArgument("x-dead-letter-routing-key", PROMPT_DLQ)
                .build();
    }

    @Bean
    public Queue promptDeadLetterQueue() {
        return QueueBuilder.durable(PROMPT_DLQ).build();
    }

    @Bean
    public DirectExchange promptExchange() {
        return new DirectExchange(PROMPT_EXCHANGE);
    }

    @Bean
    public DirectExchange promptDeadLetterExchange() {
        return new DirectExchange(PROMPT_DLX);
    }

    @Bean
    public Binding promptBinding() {
        return BindingBuilder.bind(promptQueue())
                .to(promptExchange())
                .with(PROMPT_QUEUE);
    }

    @Bean
    public Binding promptDeadLetterBinding() {
        return BindingBuilder.bind(promptDeadLetterQueue())
                .to(promptDeadLetterExchange())
                .with(PROMPT_DLQ);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("rabbitmq");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(true);
        return factory;
    }
}