package com.herofactory.prompt.dto;

import com.herofactory.prompt.entity.enums.PromptStatus;
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
