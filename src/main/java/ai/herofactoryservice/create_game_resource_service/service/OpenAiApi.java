//package ai.herofactoryservice.create_game_resource_service.service;
//
//import ai.herofactoryservice.create_game_resource_service.exception.PromptException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Getter
//public class OpenAiApi {
//    private final String apiKey;
//    private final String baseUrl;
//    private final String model;
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Builder
//    public OpenAiApi(String apiKey, String baseUrl, String model, RestTemplate restTemplate) {
//        this.apiKey = apiKey;
//        this.baseUrl = baseUrl;
//        this.model = model;
//        this.restTemplate = restTemplate;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    public String chat(String systemPrompt, String userPrompt) {
//        String url = baseUrl + "/chat/completions";
//
//        try {
//            HttpHeaders headers = createHeaders();
//
//            Map<String, Object> systemMessage = new HashMap<>();
//            systemMessage.put("role", "system");
//            systemMessage.put("content", systemPrompt);
//
//            Map<String, Object> userMessage = new HashMap<>();
//            userMessage.put("role", "user");
//            userMessage.put("content", userPrompt);
//
//            List<Map<String, Object>> messages = new ArrayList<>();
//            messages.add(systemMessage);
//            messages.add(userMessage);
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("model", model);
//            requestBody.put("messages", messages);
//            requestBody.put("temperature", 0.7);
//            requestBody.put("max_tokens", 2048);
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<JsonNode> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    requestEntity,
//                    JsonNode.class
//            );
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                return response.getBody()
//                        .path("choices")
//                        .get(0)
//                        .path("message")
//                        .path("content")
//                        .asText();
//            }
//
//            throw new PromptException("OpenAI API 응답 처리 실패");
//
//        } catch (Exception e) {
//            log.error("OpenAI API 호출 중 오류 발생", e);
//            throw new PromptException("OpenAI API 호출 실패: " + e.getMessage(), e);
//        }
//    }
//
//    public double[] embeddings(String text) {
//        String url = baseUrl + "/embeddings";
//
//        try {
//            HttpHeaders headers = createHeaders();
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("model", "text-embedding-3-small");
//            requestBody.put("input", text);
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<JsonNode> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    requestEntity,
//                    JsonNode.class
//            );
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                JsonNode embeddingData = response.getBody()
//                        .path("data")
//                        .get(0)
//                        .path("embedding");
//
//                double[] embeddings = new double[embeddingData.size()];
//                for (int i = 0; i < embeddingData.size(); i++) {
//                    embeddings[i] = embeddingData.get(i).asDouble();
//                }
//                return embeddings;
//            }
//
//            throw new PromptException("임베딩 생성 실패");
//
//        } catch (Exception e) {
//            log.error("임베딩 생성 중 오류 발생", e);
//            throw new PromptException("임베딩 생성 실패: " + e.getMessage(), e);
//        }
//    }
//
//    private HttpHeaders createHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(apiKey);
//        return headers;
//    }
//
//    public String enhancePromptForImageGeneration(String originalPrompt) {
//        String systemPrompt = """
//                당신은 전문적인 사진작가이자 이미지 생성 전문가입니다.
//                주어진 프롬프트를 다음 요소들을 고려하여 더 상세하고 전문적인 이미지 생성 프롬프트로 개선해주세요:
//
//                1. 구도와 프레이밍
//                   - 피사체의 위치와 크기
//                   - 화면 구성과 앵글
//
//                2. 기술적 특성
//                   - 파일 형식과 처리 방식
//                   - 이미지 품질과 해상도
//
//                3. 촬영 맥락
//                   - 촬영 환경과 스타일
//                   - 장르적 특성
//
//                4. 시대적/용도 맥락
//                   - 시대적 참조
//                   - 의도된 사용 목적
//
//                5. 조명 설정
//                   - 빛의 특성과 방향
//                   - 그림자와 하이라이트
//
//                6. 카메라 설정
//                   - 렌즈 선택과 효과
//                   - 심도와 초점
//
//                개선된 프롬프트는 자연스러운 문장으로 작성하되,
//                각 요소가 명확하게 전달되도록 해주세요.
//                """;
//
//        return chat(systemPrompt, originalPrompt);
//    }
//
//    public List<Map<String, List<String>>> suggestPromptKeywords(String prompt) {
//        String systemPrompt = """
//                당신은 전문적인 사진 및 이미지 생성 전문가입니다.
//                주어진 프롬프트를 분석하여 다음 카테고리별로 적절한 키워드를 추천해주세요.
//
//                각 카테고리별 고려사항:
//
//                1. Framing (구도):
//                   - 예: extreme close-up, medium shot, wide shot, dutch angle
//                   - 피사체의 위치와 크기
//                   - 화면 구성 방식
//
//                2. File Type (파일 형식):
//                   - 예: RAW, JPEG, analog film, 35mm, medium format
//                   - 이미지의 기술적 특성
//                   - 필름이나 센서 특성
//
//                3. Shoot Context (촬영 맥락):
//                   - 예: street photography, studio shot, documentary, editorial
//                   - 촬영 환경과 목적
//                   - 장르적 특성
//
//                4. Year & Usage Context (시대 및 용도 맥락):
//                   - 예: vintage 1960s, modern 2024, retro aesthetic
//                   - 시대적 특성
//                   - 사용 목적과 스타일
//
//                5. Lighting Prompt (조명):
//                   - 예: golden hour, harsh shadows, soft diffused light
//                   - 자연광/인공광 특성
//                   - 조명 기법과 효과
//
//                6. Lens & Camera Prompt (렌즈 및 카메라):
//                   - 예: wide-angle lens, telephoto compression, shallow depth of field
//                   - 카메라 장비 특성
//                   - 광학적 효과
//
//                각 카테고리별로 2-3개의 가장 적절한 키워드를 추천해주세요.
//                응답은 각 카테고리별로 구분하여 명확하게 제시해주세요.
//                키워드는 영어로 작성해주세요.
//                """;
//
//        String response = chat(systemPrompt, prompt);
//        return parseKeywordResponse(response);
//    }
//
//    private List<Map<String, List<String>>> parseKeywordResponse(String response) {
//        try {
//            List<Map<String, List<String>>> categorizedKeywords = new ArrayList<>();
//            String[] categories = {
//                    "Framing", "File Type", "Shoot Context",
//                    "Year & Usage Context", "Lighting Prompt", "Lens & Camera Prompt"
//            };
//
//            // OpenAI의 응답을 파싱하여 카테고리별로 정리
//            for (String category : categories) {
//                Map<String, List<String>> categoryMap = new HashMap<>();
//                List<String> keywords = extractKeywordsForCategory(response, category);
//                categoryMap.put(category, keywords);
//                categorizedKeywords.add(categoryMap);
//            }
//
//            return categorizedKeywords;
//        } catch (Exception e) {
//            log.error("키워드 파싱 중 오류 발생", e);
//            throw new PromptException("키워드 파싱 실패: " + e.getMessage(), e);
//        }
//    }
//
//    private List<String> extractKeywordsForCategory(String response, String category) {
//        // 정규식을 사용하여 각 카테고리의 키워드 추출
//        String pattern = category + ".*?:(.+?)(?=\\n|$)";
//        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern,
//                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
//        java.util.regex.Matcher m = r.matcher(response);
//
//        if (m.find()) {
//            String keywordStr = m.group(1).trim();
//            return Arrays.stream(keywordStr.split(","))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .collect(Collectors.toList());
//        }
//        return new ArrayList<>();
//    }
//}
//
