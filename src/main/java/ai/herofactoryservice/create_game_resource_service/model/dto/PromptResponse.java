package ai.herofactoryservice.create_game_resource_service.model.dto;

import ai.herofactoryservice.create_game_resource_service.model.PromptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {
    private String promptId;
    private String enhancedPrompt;
    private List<String> recommendedKeywords;
    private PromptStatus status;
    private String errorMessage;
}

