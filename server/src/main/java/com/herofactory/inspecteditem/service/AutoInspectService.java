package com.herofactory.inspecteditem.service;

import com.herofactory.config.openai.ChatGptClient;
import com.herofactory.config.openai.ChatGptClient.ChatPolicy;
import com.herofactory.inspecteditem.dto.AutoInspectionResult;
import com.herofactory.kafka.CustomObjectMapper;
import com.herofactory.shop.dto.ItemDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoInspectService {
    private final ChatGptClient chatGptClient;
    private final CustomObjectMapper objectMapper = new CustomObjectMapper();

    public AutoInspectionResult inspect(ItemDto itemDto) {
        String contentString = buildContentString(itemDto);
        ChatPolicy chatPolicy = new ChatPolicy(
                AutoInspectionPolicy.INSPECTION_INSTRUCTION,
                AutoInspectionPolicy.EXAMPLE_CONTENT,
                AutoInspectionPolicy.EXAMPLE_INSPECTION_RESULT

        );
        String resultString = chatGptClient.getResultForContentWithPolicy(
                contentString,
                chatPolicy
        );
        try {
            return objectMapper.readValue(resultString, AutoInspectionResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildContentString(ItemDto itemDto) {
        return String.format(
                "[%s] %s - %s", //
                itemDto.getCategoryName(),
                itemDto.getName(),
                itemDto.getDescription()
        );
    }

    static class AutoInspectionPolicy {
        private static final String INSPECTION_INSTRUCTION =
                "The task you need to accomplish is to return two items ('status' and 'tags') in JSON format. " +
                        "The information I will provide will be in the format '[CategoryName] ItemName - ItemDescription.' " +
                        "For weapons category, items like swords, axes, spears, bows are appropriate. " +  // 카테고리 기준 명시
                        "Then, if the item's name and description are appropriate for the given CategoryName, " +
                        "fill the 'status' field with the string 'GOOD.' " +
                        "If the item does not belong in that category, " +
                        "fill the 'status' field with the string 'BAD.' " +
                        "Additionally, extract and compile a list of up to 5 keywords " +
                        "that seem most important from both ItemName and ItemDescription and populate the 'tags' field with them.";
        private static final String EXAMPLE_CONTENT =
                "[무기] 강철검 - 단단한 강철로 만든 회색의 한손검입니다.";

        private static final String EXAMPLE_INSPECTION_RESULT =
                "{\"status\":\"GOOD\",\"tags\":[\"강철\", \"검\", \"회색\", \"한손검\"]}";
    }
}
