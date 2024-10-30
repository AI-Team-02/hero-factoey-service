package ai.herofactoryservice.create_game_resource_service.model.dto;

import ai.herofactoryservice.create_game_resource_service.model.PromptStatus;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {
    private String memberId;
    private String originalPrompt;
    private String sketchData;  // Base64 인코딩된 스케치 데이터
}