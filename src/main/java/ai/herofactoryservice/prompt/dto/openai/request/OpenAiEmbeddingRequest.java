package ai.herofactoryservice.prompt.dto.openai.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiEmbeddingRequest {
    private String model;
    private String input;
}
