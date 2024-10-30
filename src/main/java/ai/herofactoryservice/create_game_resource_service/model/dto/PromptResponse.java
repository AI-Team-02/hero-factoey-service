package ai.herofactoryservice.create_game_resource_service.model.dto;

import ai.herofactoryservice.create_game_resource_service.model.PromptStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {
    private String promptId;
    private String originalPrompt;
    private String improvedPrompt;
    private List<String> recommendedKeywords;
    private List<Map<String, List<String>>> categoryKeywords;
    private PromptStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}