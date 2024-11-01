//package ai.herofactoryservice.create_game_resource_service.config;
//
//import ai.herofactoryservice.create_game_resource_service.service.OpenAiApi;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//
//import java.time.Duration;
//
//@Configuration
//public class OpenAIConfig {
//
//    @Value("${openai.api-key}")
//    private String openaiApiKey;
//
//    @Value("${openai.model:gpt-4}")
//    private String model;
//
//    @Bean
//    public RestTemplate openaiRestTemplate(RestTemplateBuilder builder) {
//        return builder
//                .setConnectTimeout(Duration.ofSeconds(10))
//                .setReadTimeout(Duration.ofSeconds(30))
//                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
//                .defaultHeader("Content-Type", "application/json")
//                .build();
//    }
//
//    @Bean
//    public OpenAiApi openAiApi(RestTemplate openaiRestTemplate) {
//        return OpenAiApi.builder()
//                .apiKey(openaiApiKey)
//                .baseUrl("https://api.openai.com/v1")
//                .model(model)
//                .restTemplate(openaiRestTemplate)
//                .build();
//    }
//}