package com.herofactory.prompt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.herofactory.prompt.dto.request.OpenAiChatRequest.ChatMessage;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Integer index;
        private ChatMessage message;
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
