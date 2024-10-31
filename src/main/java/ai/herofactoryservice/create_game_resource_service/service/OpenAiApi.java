package ai.herofactoryservice.create_game_resource_service.service;

import ai.herofactoryservice.create_game_resource_service.exception.PromptException;
import ai.herofactoryservice.create_game_resource_service.exception.RateLimitException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class OpenAiApi {
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter = RateLimiter.create(20.0); // 분당 20개 요청으로 제한

    @Builder
    public OpenAiApi(String apiKey, String baseUrl, String model, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<String> chatAsync(String systemPrompt, String userPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            if (!rateLimiter.tryAcquire()) {
                throw new RateLimitException("API 호출 한도 초과");
            }
            return chat(systemPrompt, userPrompt);
        });
    }

    public String chat(String systemPrompt, String userPrompt) {
        String url = baseUrl + "/chat/completions";

        try {
            Map<String, Object> requestBody = createChatRequestBody(systemPrompt, userPrompt);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, createHeaders());

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractContentFromResponse(response.getBody());
            }

            throw new PromptException("OpenAI API 응답 처리 실패");

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("OpenAI API 호출 한도 초과. 잠시 후 재시도합니다.");
            // 적절한 대기 시간 후 재시도 로직 구현 가능
            throw new PromptException("API 호출 한도 초과", e);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            throw new PromptException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createChatRequestBody(String systemPrompt, String userPrompt) {
        Map<String, Object> systemMessage = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> userMessage = Map.of("role", "user", "content", userPrompt);

        return Map.of(
                "model", model,
                "messages", List.of(systemMessage, userMessage),
                "temperature", 0.7,
                "max_tokens", 2048
        );
    }

    private String extractContentFromResponse(JsonNode responseBody) {
        return responseBody
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }

    public CompletableFuture<double[]> embeddingsAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            if (!rateLimiter.tryAcquire()) {
                throw new RateLimitException("API 호출 한도 초과");
            }
            return embeddings(text);
        });
    }

    public double[] embeddings(String text) {
        String url = baseUrl + "/embeddings";

        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "text-embedding-3-small");
            requestBody.put("input", text);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode embeddingData = response.getBody()
                        .path("data")
                        .get(0)
                        .path("embedding");

                double[] embeddings = new double[embeddingData.size()];
                for (int i = 0; i < embeddingData.size(); i++) {
                    embeddings[i] = embeddingData.get(i).asDouble();
                }
                return embeddings;
            }

            throw new PromptException("임베딩 생성 실패");

        } catch (Exception e) {
            log.error("임베딩 생성 중 오류 발생", e);
            throw new PromptException("임베딩 생성 실패: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    // 통합된 프롬프트 분석 메서드
    public String analyzePrompt(String prompt) {
        String systemPrompt = """
            다음 프롬프트에 대해 한 번의 분석으로 다음 정보를 모두 제공해주세요:
            
            1. 핵심 키워드 (최대 10개, 쉼표로 구분)
            2. 개선된 프롬프트
            3. 카테고리별 추천 키워드
            
            응답 형식:
            ---KEYWORDS---
            키워드1, 키워드2, ...
            ---IMPROVED---
            개선된 프롬프트
            ---CATEGORIES---
            Framing: keyword1, keyword2
            File Type: keyword1, keyword2
            Shoot Context: keyword1, keyword2
            ...
            """;

        return chat(systemPrompt, prompt);
    }
}