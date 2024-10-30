package ai.herofactoryservice.create_game_resource_service.config;

import ai.herofactoryservice.create_game_resource_service.service.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")  // 경로 수정
    private String openaiApiKey;

    @Value("${openai.api.models.chat:gpt-4}")  // 경로 수정
    private String model;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")  // 경로 추가
    private String baseUrl;

    @Value("${openai.api.request.timeout:60000}")  // timeout 설정 추가
    private int timeout;

    @Bean
    public RestTemplate openaiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public OpenAiApi openAiApi(RestTemplate openaiRestTemplate) {
        return OpenAiApi.builder()
                .apiKey(openaiApiKey)
                .baseUrl(baseUrl)
                .model(model)
                .restTemplate(openaiRestTemplate)
                .build();
    }
}