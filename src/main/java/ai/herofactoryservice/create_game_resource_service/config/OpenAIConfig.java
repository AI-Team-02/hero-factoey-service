package ai.herofactoryservice.create_game_resource_service.config;

import ai.herofactoryservice.create_game_resource_service.interceptor.RateLimitingInterceptor;
import ai.herofactoryservice.create_game_resource_service.service.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.base-url}")
    private String baseUrl;

    @Value("${openai.api.models.chat}")
    private String chatModel;

    @Value("${openai.api.models.embedding}")
    private String embeddingModel;

    @Value("${openai.api.rate-limit.requests-per-minute}")
    private double requestsPerMinute;


    @Bean
    public OpenAiApi openAiApi(RestTemplate openaiRestTemplate) {
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .model(chatModel)
                .embeddingModel(embeddingModel)
                .restTemplate(openaiRestTemplate)
                .requestsPerMinute(requestsPerMinute)
                .build();
    }
}