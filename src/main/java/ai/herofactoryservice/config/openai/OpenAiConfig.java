package ai.herofactoryservice.config.openai;

import ai.herofactoryservice.prompt.infrastructure.openai.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OpenAiConfig {

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
    public OpenAiApi openAiApi(@Qualifier("openAiRestTemplate") RestTemplate openaiRestTemplate) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.startsWith("sk-")) {
            log.error("Invalid OpenAI API key configuration");
            throw new IllegalStateException("Invalid OpenAI API key");
        }

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .model(chatModel)
                .embeddingModel(embeddingModel)
                .restTemplate(openaiRestTemplate)
                .requestsPerMinute(requestsPerMinute)
                .build();

        log.info("Initialized OpenAI API with models - Chat: {}, Embedding: {}", chatModel, embeddingModel);

        return api;
    }
}