package ai.herofactoryservice.create_game_resource_service.service;

import ai.herofactoryservice.create_game_resource_service.exception.PromptException;
import ai.herofactoryservice.create_game_resource_service.messaging.PromptProducer;
import ai.herofactoryservice.create_game_resource_service.model.Prompt;
import ai.herofactoryservice.create_game_resource_service.model.PromptStatus;
import ai.herofactoryservice.create_game_resource_service.model.PromptMessage;
import ai.herofactoryservice.create_game_resource_service.model.dto.PromptRequest;
import ai.herofactoryservice.create_game_resource_service.model.dto.PromptResponse;
import ai.herofactoryservice.create_game_resource_service.repository.CustomVectorRepository;
import ai.herofactoryservice.create_game_resource_service.repository.PromptRepository;
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
                    .updatedAt(now)  // updatedAt 명시적 설정
                    .keywords(new ArrayList<>())  // 빈 리스트로 초기화
                    .categoryKeywords(new ArrayList<>())  // 빈 리스트로 초기화
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
        String[] sections = response.split("---\\w+---");

        List<String> keywords = Arrays.asList(sections[1].trim().split("\\s*,\\s*"));
        String improvedPrompt = sections[2].trim();
        Map<String, List<String>> categoryKeywords = parseCategoryKeywords(sections[3]);

        return new ProcessedPromptData(keywords, improvedPrompt, categoryKeywords);
    }

    private Map<String, List<String>> parseCategoryKeywords(String categorySection) {
        Map<String, List<String>> result = new HashMap<>();
        String[] lines = categorySection.trim().split("\n");

        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String category = parts[0].trim();
                List<String> keywords = Arrays.asList(parts[1].trim().split("\\s*,\\s*"));
                result.put(category, keywords);
            }
        }

        return result;
    }

    private void updatePromptWithResults(Prompt prompt, ProcessedPromptData data, double[] embedding) {
        prompt.setImprovedPrompt(data.improvedPrompt());
        prompt.setKeywords(data.keywords());
        prompt.setKeywords(data.keywords());
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