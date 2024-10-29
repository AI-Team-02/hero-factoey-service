package ai.herofactoryservice.create_game_resource_service.service;

import ai.herofactoryservice.create_game_resource_service.exception.PromptException;
import ai.herofactoryservice.create_game_resource_service.messaging.PromptProducer;
import ai.herofactoryservice.create_game_resource_service.model.*;
import ai.herofactoryservice.create_game_resource_service.model.dto.*;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptService {
    private final PromptRepository promptRepository;
    private final PromptProducer promptProducer;
    private final OpenAiApi openAiApi;
    private final PlatformTransactionManager transactionManager;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PromptResponse createPrompt(PromptRequest request) {
        String promptId = UUID.randomUUID().toString();

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // 프롬프트 엔티티 생성
            Prompt prompt = Prompt.builder()
                    .promptId(promptId)
                    .memberId(request.getMemberId())
                    .originalPrompt(request.getOriginalPrompt())
                    .status(PromptStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            promptRepository.save(prompt);

            // 프롬프트 처리 메시지 생성 및 전송
            PromptMessage message = createPromptMessage(prompt, request.getSketchData());
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
                    .orElseThrow(() -> new PromptException("프롬프트 정보를 찾을 수 없습니다."));

            if (!prompt.getStatus().canProcess()) {
                throw new PromptException("처리할 수 없는 상태의 프롬프트입니다.");
            }

            prompt.setStatus(PromptStatus.PROCESSING);
            promptRepository.save(prompt);

            // 프롬프트 분석 및 개선
            List<String> keywords = extractKeywords(prompt.getOriginalPrompt());
            String enhancedPrompt = enhancePrompt(prompt.getOriginalPrompt(), keywords);
            Float[] embedding = generateEmbedding(enhancedPrompt);

            // 유사한 프롬프트 검색 및 추가 키워드 추천
            List<String> additionalKeywords = findAdditionalKeywords(embedding);
            keywords.addAll(additionalKeywords);

            // 결과 저장
            prompt.setEnhancedPrompt(enhancedPrompt);
            prompt.setEmbedding(embedding);
            prompt.setKeywords(keywords);
            prompt.setStatus(PromptStatus.COMPLETED);
            prompt.setCompletedAt(LocalDateTime.now());
            prompt.setUpdatedAt(LocalDateTime.now());

            promptRepository.save(prompt);

            transactionManager.commit(status);

        } catch (Exception e) {
            transactionManager.rollback(status);
            handlePromptProcessingError(message.getPromptId(), e);
            throw new PromptException("프롬프트 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public PromptResponse getPromptStatus(String promptId) {
        Prompt prompt = promptRepository.findByPromptId(promptId)
                .orElseThrow(() -> new PromptException("프롬프트 정보를 찾을 수 없습니다."));

        return createPromptResponse(prompt);
    }

    // Private helper methods
    private PromptMessage createPromptMessage(Prompt prompt, String sketchData) {
        return PromptMessage.builder()
                .promptId(prompt.getPromptId())
                .memberId(prompt.getMemberId())
                .originalPrompt(prompt.getOriginalPrompt())
                .sketchData(sketchData)
                .status(prompt.getStatus())
                .build();
    }

    private PromptResponse createPromptResponse(Prompt prompt) {
        return PromptResponse.builder()
                .promptId(prompt.getPromptId())
                .enhancedPrompt(prompt.getEnhancedPrompt())
                .recommendedKeywords(prompt.getKeywords())
                .status(prompt.getStatus())
                .errorMessage(prompt.getErrorMessage())
                .build();
    }

    private List<String> extractKeywords(String originalPrompt) {
        String systemPrompt = """
            입력된 프롬프트에서 이미지 생성에 중요한 키워드를 추출하세요.
            스타일, 구도, 색감, 주요 객체 등을 고려하여 최대 10개의 키워드를 추출하세요.
            각 키워드는 쉼표로 구분하여 반환하세요.
            """;

        String response = openAiApi.chat(systemPrompt, originalPrompt);
        return List.of(response.split(","));
    }

    private String enhancePrompt(String originalPrompt, List<String> keywords) {
        String systemPrompt = """
            주어진 프롬프트를 이미지 생성에 최적화된 형태로 개선하세요.
            추출된 키워드를 활용하여 더 상세하고 명확한 프롬프트를 생성하세요.
            이미지의 스타일, 구도, 색감, 주요 객체 등이 명확하게 드러나도록 작성하세요.
            """;

        String promptWithKeywords = originalPrompt + "\n추출된 키워드: " + String.join(", ", keywords);
        return openAiApi.chat(systemPrompt, promptWithKeywords);
    }

    private Float[] generateEmbedding(String text) {
        return openAiApi.embeddings(text);
    }

    private List<String> findAdditionalKeywords(Float[] embedding) {
        List<Prompt> similarPrompts = promptRepository.findSimilarPrompts(embedding, 5);
        return similarPrompts.stream()
                .flatMap(p -> p.getKeywords().stream())
                .distinct()
                .limit(5)
                .toList();
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