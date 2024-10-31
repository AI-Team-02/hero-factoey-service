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
    private String openaiApiKey;

    @Value("${openai.api.models.chat:gpt-4}")
    private String model;

    @Value("${openai.api.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api.request.timeout:60000}")
    private int timeout;

    @Value("${openai.api.rate-limit.requests-per-minute:20}")
    private double requestsPerMinute;

    @Bean
    public RestTemplate openaiRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RateLimitingInterceptor(requestsPerMinute));
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
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