package ai.herofactoryservice.prompt.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {
    private String memberId;
    private String originalPrompt;
    private String sketchData;  // Base64 인코딩된 스케치 데이터
}