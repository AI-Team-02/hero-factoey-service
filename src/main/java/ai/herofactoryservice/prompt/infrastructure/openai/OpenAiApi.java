package ai.herofactoryservice.prompt.infrastructure.openai;

import ai.herofactoryservice.common.exception.PromptException;
import ai.herofactoryservice.common.exception.RateLimitException;
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

@Slf4j
@Getter
public class OpenAiApi {
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final String embeddingModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;

    public static final String ANALYSIS_SYSTEM_PROMPT = """
    You are an expert AI image prompt engineer specializing in both photography and digital art. Your task is to analyze and transform the given Korean prompt into a highly detailed, professional prompt that will produce exceptional AI-generated images.
    
    Follow these detailed steps for analysis and enhancement:
    
    1. Core Analysis:
       - Main Subject: Identify key elements, features, and characteristics
       - Style: Determine overall artistic direction and visual approach
       - Mood: Analyze emotional tone and atmosphere
       - Technical Requirements: Identify necessary technical specifications
    
    2. Detailed Enhancement Categories:
    
       Technical Quality (must include):
       - Resolution specs (e.g., 8K, ultra HD, high resolution)
       - Quality indicators (masterpiece, best quality, professional photography)
       - Rendering specifics (e.g., octane render, unreal engine 5, ray tracing)
       - Post-processing effects (HDR, tone mapping, color grading)
    
       Visual Elements (incorporate at least 4):
       - Lighting setup (volumetric lighting, rim light, global illumination)
       - Color palette (precise color schemes, e.g., "deep crimson and burnished gold")
       - Material properties (subsurface scattering, metallic reflections)
       - Texture details (fabric patterns, surface imperfections)
       - Visual effects (lens flare, bokeh, motion blur)
    
       Artistic Direction (include minimum 3):
       - Art movement influences (e.g., hyperrealism, baroque, cyberpunk)
       - Artist references (style of specific artists/photographers)
       - Time period/cultural elements
       - Artistic techniques (brush strokes, digital painting style)
    
       Composition (specify at least 3):
       - Camera angle (exact degree if applicable)
       - Shot type (close-up, medium shot, wide angle)
       - Focal length (specific mm lens)
       - Perspective (isometric, fish-eye, bird's eye)
       - Framing elements (rule of thirds, golden ratio)
    
       Environment & Atmosphere (detail minimum 3):
       - Time of day (specific lighting conditions)
       - Weather elements (precise atmospheric effects)
       - Environmental context (location details)
       - Ambient effects (mist, particles, reflections)
    
       Technical Parameters (always include):
       - Aspect ratio
       - Style modifiers (trending on artstation, award winning)
       - Quality modifiers (professional, masterpiece)
    
    3. Negative Prompts:
       Specify elements to avoid, including:
       - Technical issues (blur, noise, artifacts)
       - Composition problems (bad framing, wrong perspective)
       - Quality issues (low resolution, pixelation)
    
    Response Format:
    ---ANALYSIS---
    Subject: [Detailed subject description]
    Core Style: [Primary artistic direction]
    Mood: [Atmospheric and emotional elements]
    
    ---ENHANCED KEYWORDS---
    [Detailed keywords organized by category, with both Korean and English]
    
    ---IMPROVED PROMPT---
    [Comprehensive Korean prompt incorporating all essential elements]
    
    [Structured English prompt with this format:]
    {main subject description}, {technical quality}, {visual elements}, {artistic style}, {composition}, {environment}, {technical parameters}, {additional style modifiers}
    
    ---NEGATIVE PROMPTS---
    Korean: [Detailed list of elements to avoid in Korean]
    English: [Comprehensive list of elements to avoid in English]
    
    ---REFERENCE EXAMPLES---
    1. [Similar successful prompt example]
    2. [Alternative approach example]
    3. [Variation example]
    
    Remember to:
    - Be extremely specific with technical terms
    - Use precise descriptive language
    - Maintain logical flow in prompt structure
    - Include all required technical parameters
    - Balance artistic and technical elements
    - Ensure coherence between all elements
    """;

    @Builder
    public OpenAiApi(
            String apiKey,
            String baseUrl,
            String model,
            String embeddingModel,
            RestTemplate restTemplate,
            double requestsPerMinute) {
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.startsWith("sk-")) {
            throw new IllegalArgumentException("유효하지 않은 OpenAI API 키입니다.");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.embeddingModel = embeddingModel;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.rateLimiter = RateLimiter.create(requestsPerMinute);

        // API 키 검증 로그
        log.info("OpenAI API initialized with models - Chat: {}, Embedding: {}",
                model, embeddingModel);
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
            // API 키 검증
            if (apiKey == null || apiKey.isEmpty() || !apiKey.startsWith("sk-")) {
                throw new PromptException("유효하지 않은 OpenAI API 키입니다.");
            }

            Map<String, Object> requestBody = createChatRequestBody(systemPrompt, userPrompt);
            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 디버그 로깅 추가
            log.debug("OpenAI API Request - URL: {}, Model: {}, Headers: {}",
                    url, model, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String content = extractContentFromResponse(response.getBody());
                log.debug("OpenAI API Response received successfully");
                return content;
            }

            throw new PromptException("OpenAI API 응답이 비어있습니다.");

        } catch (HttpClientErrorException e) {
            log.error("OpenAI API Error - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            handleOpenAiError(e, "chat");
            throw new PromptException("OpenAI API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during OpenAI API call", e);
            throw new PromptException("OpenAI API 호출 중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }

    private Map<String, Object> createChatRequestBody(String systemPrompt, String userPrompt) {
        Map<String, Object> systemMessage = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> userMessage = Map.of("role", "user", "content", userPrompt);

        double temperature = userPrompt.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*") ? 0.5 : 0.7;

        return Map.of(
                "model", model,
                "messages", List.of(systemMessage, userMessage),
                "temperature", temperature,
                "max_tokens", 3072,
                "presence_penalty", 0.1,
                "frequency_penalty", 0.1
        );
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
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("input", text);

            if (log.isDebugEnabled()) {
                log.debug("Embedding request - Model: {}, Input length: {}",
                        embeddingModel, text.length());
            }

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, createHeaders());

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractEmbeddingFromResponse(response.getBody());
            }

            throw new PromptException("임베딩 생성 실패");

        } catch (HttpClientErrorException e) {
            handleOpenAiError(e, "embedding");
            throw new PromptException("Unexpected error");
        } catch (Exception e) {
            log.error("임베딩 생성 중 오류 발생", e);
            throw new PromptException("임베딩 생성 실패: " + e.getMessage(), e);
        }
    }

    private void handleOpenAiError(HttpClientErrorException e, String operation) {
        String responseBody = e.getResponseBodyAsString();
        HttpStatusCode statusCode = e.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        try {
            JsonNode errorResponse = objectMapper.readTree(responseBody);
            JsonNode error = errorResponse.path("error");
            String errorMessage = error.path("message").asText();
            String errorType = error.path("type").asText();
            String errorCode = error.path("code").asText();

            log.error("{} API Error - Type: {}, Code: {}, Message: {}",
                    operation, errorType, errorCode, errorMessage);

            if ("model_not_found".equals(errorCode)) {
                throw new PromptException(String.format(
                        "모델 접근 권한이 없습니다 (%s): %s",
                        operation.equals("embedding") ? embeddingModel : model,
                        errorMessage
                ));
            }

            if ("invalid_request_error".equals(errorType)) {
                throw new PromptException("잘못된 요청입니다: " + errorMessage);
            }
        } catch (PromptException pe) {
            throw pe;
        } catch (Exception parseError) {
            log.error("Error parsing OpenAI error response", parseError);
        }

        if (status == null) {
            throw new PromptException(String.format("%s API 호출 중 알 수 없는 HTTP 상태 코드 %d 발생: %s",
                    operation, statusCode.value(), responseBody));
        }

        switch (status) {
            case UNAUTHORIZED -> throw new PromptException("API 키가 유효하지 않습니다: " + responseBody);
            case FORBIDDEN -> throw new PromptException("API 접근 권한이 없습니다. OpenAI API 키와 모델 접근 권한을 확인해주세요: " + responseBody);
            case TOO_MANY_REQUESTS -> throw new RateLimitException("API 호출 한도를 초과했습니다: " + responseBody);
            case BAD_REQUEST -> throw new PromptException("잘못된 요청입니다: " + responseBody);
            default -> throw new PromptException("API 호출 중 오류가 발생했습니다 (" + status + "): " + responseBody);
        }
    }

    private String extractContentFromResponse(JsonNode responseBody) {
        return responseBody
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }

    private double[] extractEmbeddingFromResponse(JsonNode responseBody) {
        try {
            JsonNode embeddingData = responseBody
                    .path("data")
                    .get(0)
                    .path("embedding");

            double[] embeddings = new double[embeddingData.size()];
            for (int i = 0; i < embeddingData.size(); i++) {
                embeddings[i] = embeddingData.get(i).asDouble();
            }
            return embeddings;
        } catch (Exception e) {
            log.error("임베딩 응답 파싱 중 오류 발생", e);
            throw new PromptException("임베딩 응답 파싱 실패", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    public String analyzePrompt(String prompt) {
        return chat(ANALYSIS_SYSTEM_PROMPT, prompt);
    }
}