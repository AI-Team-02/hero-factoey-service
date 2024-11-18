package com.herofactory.inspecteditem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private ChatChoice[] choices;
    private Usage usage;
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    @Data
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatChoice {
        private int index;
        private Message message;
        private Object logprobs;
        @JsonProperty("finish_reason")
        private String finishReason;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String role;
            private String content;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;

    }
}