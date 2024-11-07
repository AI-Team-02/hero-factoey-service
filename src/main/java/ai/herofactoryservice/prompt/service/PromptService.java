package ai.herofactoryservice.prompt.service;

import ai.herofactoryservice.common.exception.PromptException;
import ai.herofactoryservice.prompt.infrastructure.messaging.producer.PromptProducer;
import ai.herofactoryservice.prompt.entity.Prompt;
import ai.herofactoryservice.prompt.entity.enums.PromptStatus;
import ai.herofactoryservice.prompt.dto.PromptMessage;
import ai.herofactoryservice.prompt.dto.request.PromptRequest;
import ai.herofactoryservice.prompt.dto.response.PromptResponse;
import ai.herofactoryservice.prompt.infrastructure.openai.OpenAiApi;
import ai.herofactoryservice.prompt.repository.CustomVectorRepository;
import ai.herofactoryservice.prompt.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptService {
    private final PromptRepository promptRepository;
    private final CustomVectorRepository customVectorRepository;
    private final PromptProducer promptProducer;
    private final OpenAiApi openAiApi;
    private final PlatformTransactionManager transactionManager;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PromptResponse createPrompt(PromptRequest request) {
        String promptId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Prompt prompt = Prompt.builder()
                    .promptId(promptId)
                    .memberId(request.getMemberId())
                    .originalPrompt(request.getOriginalPrompt())
                    .status(PromptStatus.PENDING)
                    .createdAt(now)
                    .updatedAt(now)
                    .keywords(new ArrayList<>())
                    .categoryKeywords(new ArrayList<>())
                    .build();

            customVectorRepository.savePromptWithVector(prompt);

            PromptMessage message = createPromptMessage(prompt);
            promptProducer.sendPromptMessage(message);

            transactionManager.commit(status);
            return createPromptResponse(prompt);

        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("프롬프트 생성 중 오류 발생", e);
            throw new PromptException("프롬프트 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void processPrompt(PromptMessage message) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Prompt prompt = promptRepository.findByPromptIdWithLock(message.getPromptId())
                    .orElseThrow(() -> new PromptException("프롬프트를 찾을 수 없습니다."));

            if (!prompt.getStatus().canProcess()) {
                return;
            }

            prompt.setStatus(PromptStatus.PROCESSING);
            customVectorRepository.savePromptWithVector(prompt);

            CompletableFuture<String> analysisFuture = openAiApi.chatAsync(
                    OpenAiApi.ANALYSIS_SYSTEM_PROMPT,
                    prompt.getOriginalPrompt()
            );

            CompletableFuture<double[]> embeddingFuture = openAiApi.embeddingsAsync(
                    prompt.getOriginalPrompt()
            );

            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    analysisFuture, embeddingFuture
            );

            try {
                allOf.get(45, TimeUnit.SECONDS);

                String analysis = analysisFuture.get();
                double[] embedding = embeddingFuture.get();

                ProcessedPromptData processedData = parseProcessedData(analysis);
                updatePromptWithResults(prompt, processedData, embedding);

                prompt.setStatus(PromptStatus.COMPLETED);
                customVectorRepository.savePromptWithVector(prompt);

                transactionManager.commit(status);

            } catch (Exception e) {
                throw new PromptException("API 처리 중 오류 발생: " + e.getMessage());
            }

        } catch (Exception e) {
            transactionManager.rollback(status);
            handlePromptProcessingError(message.getPromptId(), e);
            throw new PromptException("프롬프트 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PromptResponse getPromptStatus(String promptId) {
        Prompt prompt = customVectorRepository.findByPromptId(promptId)
                .orElseThrow(() -> new PromptException("프롬프트 정보를 찾을 수 없습니다."));
        return createPromptResponse(prompt);
    }

    private record ProcessedPromptData(
            List<String> keywords,
            String improvedPrompt,
            Map<String, List<String>> categoryKeywords
    ) {}

    private ProcessedPromptData parseProcessedData(String response) {
        try {
            Map<String, Object> parsedData = new HashMap<>();
            String currentSection = null;
            StringBuilder sectionContent = new StringBuilder();

            // 기본값 초기화
            parsedData.put("KEYWORDS", new ArrayList<String>());
            parsedData.put("IMPROVED", "");
            parsedData.put("CATEGORIES", new HashMap<String, List<String>>());

            for (String line : response.split("\n")) {
                line = line.trim();

                // 섹션 헤더 확인
                if (line.startsWith("---") && line.endsWith("---")) {
                    // 이전 섹션 처리
                    if (currentSection != null) {
                        processSection(currentSection, sectionContent.toString().trim(), parsedData);
                    }

                    // 새 섹션 시작
                    currentSection = line.replaceAll("-", "").trim();
                    sectionContent = new StringBuilder();
                } else if (!line.isEmpty() && currentSection != null) {
                    sectionContent.append(line).append("\n");
                }
            }

            // 마지막 섹션 처리
            if (currentSection != null) {
                processSection(currentSection, sectionContent.toString().trim(), parsedData);
            }

            return new ProcessedPromptData(
                    (List<String>) parsedData.get("KEYWORDS"),
                    (String) parsedData.get("IMPROVED"),
                    (Map<String, List<String>>) parsedData.get("CATEGORIES")
            );
        } catch (Exception e) {
            log.error("Error parsing response: {}", e.getMessage());
            return new ProcessedPromptData(
                    new ArrayList<>(),
                    "",
                    new HashMap<>()
            );
        }
    }

    private void processSection(String section, String content, Map<String, Object> parsedData) {
        switch (section.toUpperCase().trim()) {
            case "KEYWORDS", "ENHANCED KEYWORDS" -> {
                List<String> keywords = Arrays.stream(content.split("\n"))
                        .flatMap(line -> Arrays.stream(line.split(",")))
                        .map(String::trim)
                        .filter(k -> !k.isEmpty() && !k.startsWith("[") && !k.endsWith("]"))
                        .collect(Collectors.toList());
                parsedData.put("KEYWORDS", keywords);
            }
            case "IMPROVED", "IMPROVED PROMPT" -> {
                String improvedPrompt = Arrays.stream(content.split("\n"))
                        .filter(line -> !line.toLowerCase().startsWith("english"))
                        .findFirst()
                        .map(String::trim)
                        .orElse("");
                parsedData.put("IMPROVED", improvedPrompt);
            }
            case "CATEGORIES", "ENHANCED CATEGORIES" -> {
                Map<String, List<String>> categories = new HashMap<>();
                Arrays.stream(content.split("\n"))
                        .filter(line -> line.contains(":"))
                        .forEach(line -> {
                            String[] parts = line.split(":");
                            String category = parts[0].trim();
                            List<String> keywords = parts.length > 1 ?
                                    Arrays.stream(parts[1].split("\\|")[0].split(","))
                                            .map(String::trim)
                                            .filter(k -> !k.isEmpty() && !k.startsWith("[") && !k.endsWith("]"))
                                            .collect(Collectors.toList()) :
                                    new ArrayList<>();
                            categories.put(category, keywords);
                        });
                parsedData.put("CATEGORIES", categories);
            }
            case "ANALYSIS", "REFERENCE EXAMPLES", "NEGATIVE PROMPTS" -> {
                // 이러한 섹션들은 현재 사용하지 않으므로 무시
                log.debug("Skipping optional section: {}", section);
            }
            default -> log.debug("Unknown section: {}", section);
        }
    }

    private void updatePromptWithResults(Prompt prompt, ProcessedPromptData data, double[] embedding) {
        prompt.setImprovedPrompt(data.improvedPrompt());
        prompt.setKeywords(data.keywords());
        prompt.setCategoryKeywords(new ArrayList<>(data.categoryKeywords().entrySet().stream()
                .map(entry -> Collections.singletonMap(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())));
        prompt.setEmbeddingVector(embedding);
        prompt.setCompletedAt(LocalDateTime.now());
        prompt.setUpdatedAt(LocalDateTime.now());
    }

    private PromptMessage createPromptMessage(Prompt prompt) {
        return PromptMessage.builder()
                .promptId(prompt.getPromptId())
                .memberId(prompt.getMemberId())
                .originalPrompt(prompt.getOriginalPrompt())
                .status(prompt.getStatus())
                .build();
    }

    private PromptResponse createPromptResponse(Prompt prompt) {
        return PromptResponse.builder()
                .promptId(prompt.getPromptId())
                .originalPrompt(prompt.getOriginalPrompt())
                .improvedPrompt(prompt.getImprovedPrompt())
                .recommendedKeywords(prompt.getKeywords())
                .categoryKeywords(prompt.getCategoryKeywords())
                .status(prompt.getStatus())
                .errorMessage(prompt.getErrorMessage())
                .createdAt(prompt.getCreatedAt())
                .completedAt(prompt.getCompletedAt())
                .build();
    }

    private void handlePromptProcessingError(String promptId, Exception e) {
        try {
            Prompt prompt = promptRepository.findByPromptId(promptId)
                    .orElseThrow(() -> new PromptException("프롬프트 정보를 찾을 수 없습니다."));

            prompt.setStatus(PromptStatus.FAILED);
            prompt.setErrorMessage(e.getMessage());
            prompt.setUpdatedAt(LocalDateTime.now());
            promptRepository.save(prompt);

        } catch (Exception ex) {
            log.error("프롬프트 에러 처리 중 추가 오류 발생", ex);
        }
    }
}
