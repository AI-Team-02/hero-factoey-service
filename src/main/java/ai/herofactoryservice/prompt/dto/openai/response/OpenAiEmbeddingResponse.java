package ai.herofactoryservice.prompt.dto.openai.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiEmbeddingResponse {
    private String object;
    private List<EmbeddingData> data;
    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddingData {
        private List<Float> embedding;
        private Integer index;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private Integer promptTokens;
        private Integer totalTokens;
    }
}
