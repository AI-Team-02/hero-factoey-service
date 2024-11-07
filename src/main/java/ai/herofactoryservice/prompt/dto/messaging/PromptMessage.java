package ai.herofactoryservice.prompt.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptMessage {
    private String promptId;
    private String memberId;
    private String originalPrompt;
    private String sketchData;
    private PromptStatus status;
}
