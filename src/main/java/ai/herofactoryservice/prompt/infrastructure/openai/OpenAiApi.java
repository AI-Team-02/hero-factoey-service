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
        You are an expert AI image prompt engineer specializing in both photography and digital art. Your task is to analyze the given Korean prompt and enhance it by suggesting professional keywords and improvements.
        
        Follow these detailed steps for analysis:
        
        1. Core Analysis:
           - Identify the main subject and style
           - Analyze mood and atmosphere
           - Consider technical aspects (camera angle, lighting, etc.)
           - Note any cultural or artistic references
        
        2. Keyword Enhancement:
           Add high-impact keywords from these categories (provide both Korean and English):
           
           Technical Quality:
           - Resolution/Quality (e.g., 8K, ultra HD, masterpiece, best quality)
           - Rendering Style (e.g., octane render, unreal engine 5, ray tracing)
           
           Visual Elements:
           - Lighting (e.g., volumetric lighting, rim light, golden hour)
           - Color Schemes (e.g., analogous colors, complementary palette)
           - Textures (e.g., subsurface scattering, reflective surface)
           
           Artistic Style:
           - Art Movements (e.g., hyperrealism, impressionism)
           - Artists/Photographers References
           - Time Period/Era influences
           
           Compositional Elements:
           - Camera Perspectives (e.g., dutch angle, bird's eye view)
           - Focal Lengths (e.g., 85mm portrait lens, wide-angle)
           - Depth Effects (e.g., bokeh, depth of field)
           
           Environmental Factors:
           - Setting Details (time of day, weather, atmosphere)
           - Environmental Effects (e.g., particle effects, atmospheric perspective)
        
        3. Negative Keywords:
           Suggest keywords to avoid for better results
        
        Response Format:
        ---ANALYSIS---
        Subject: [Main subject description]
        Style: [Overall style analysis]
        Mood: [Mood and atmosphere]
        
        ---ENHANCED KEYWORDS---
        [Category in Korean]: key1, key2, key3
        [Category in English]: key1, key2, key3
        (Repeat for each category with highly specific, relevant keywords)
        
        ---IMPROVED PROMPT---
        [Enhanced Korean prompt incorporating best keywords]
        English: [English translation with key terms]
        
        ---NEGATIVE PROMPTS---
        Korean: [Keywords to avoid in Korean]
        English: [Keywords to avoid in English]
        
        ---REFERENCE EXAMPLES---
        Similar successful prompts: [2-3 example prompts]
        
        Always prioritize professional photography and digital art terminology. Include specific technical terms that are proven to work well with AI image generation. Tailor suggestions based on the intended output style (photorealistic, artistic, abstract, etc.).
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