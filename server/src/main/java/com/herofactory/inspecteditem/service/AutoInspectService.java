package com.herofactory.inspecteditem.service;

import com.herofactory.config.openai.ChatGptClient;
import com.herofactory.config.openai.ChatGptClient.ChatPolicy;
import com.herofactory.inspecteditem.dto.AutoInspectionResult;
import com.herofactory.kafka.CustomObjectMapper;
import com.herofactory.shop.dto.ItemDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
                "[%s] %s - %s",
                itemDto.getCategoryName(),
                itemDto.getName(),
                itemDto.getDescription()
        );
    }

    static class AutoInspectionPolicy {

        private static final String INSPECTION_INSTRUCTION =
                "Your task is to evaluate whether an item belongs to a single given category ('무기', '방어구', '악세서리'). " +
                        "The input will be in the format '[CategoryName] ItemName - ItemDescription.' " +
                        "You must determine if the item is most appropriate for the given category. An item can belong to only one category. " +
                        "Evaluate the appropriateness of the ItemName and ItemDescription for the given CategoryName. " +
                        "Focus on the item's purpose and ignore material or secondary attributes. " +

                        "Definitions for the categories: " +
                        "1. '무기': Items designed for attack or damage (e.g., swords, bows, staffs). " +
                        "2. '방어구': Items designed for defense or protection (e.g., helmets, shields, armor). " +
                        "3. '악세서리': Items for buffs, aesthetics, or auxiliary effects (e.g., rings, necklaces, charms). " +
                        "Mark 'GOOD' if the item most clearly and exclusively fits the category. " +
                        "Mark 'BAD' if the item could belong to multiple categories or does not clearly fit the given category. " +
                        "Extract up to 5 important keywords from the ItemName and ItemDescription for the 'tags' field.";
              private static final String EXAMPLE_CONTENT =
                "[방어구] 강철 방패 - 단단한 강철로 만들어져 적의 공격을 막아줍니다.\n" +
                        "[방어구] 나무 헬멧 - 단단한 나무로 만들어진 초보자용 방어구로, 약한 공격을 방어하는 데 적합합니다.\n" +
                        "[방어구] 가죽 갑옷 - 가볍고 이동하기 편한 방어구로 초보자에게 적합합니다.\n" +
                        "[무기] 강철검 - 단단한 강철로 만들어져 적에게 피해를 입힙니다.\n" +
                        "[악세서리] 마법 반지 - 사용자의 마법력을 증가시키는 반지입니다.";

        private static final String EXAMPLE_INSPECTION_RESULT =
                "{\"status\":\"GOOD\",\"tags\":[\"강철\", \"방패\", \"공격\", \"방어\"]}\n" +
                        "{\"status\":\"GOOD\",\"tags\":[\"나무\", \"헬멧\", \"초보자\", \"방어구\"]}\n" +
                        "{\"status\":\"GOOD\",\"tags\":[\"가죽\", \"갑옷\", \"가벼운\", \"방어구\"]}\n" +
                        "{\"status\":\"GOOD\",\"tags\":[\"강철\", \"검\", \"적\", \"피해\"]}\n" +
                        "{\"status\":\"GOOD\",\"tags\":[\"마법\", \"반지\", \"마법력\", \"악세서리\"]}";


    }
}
