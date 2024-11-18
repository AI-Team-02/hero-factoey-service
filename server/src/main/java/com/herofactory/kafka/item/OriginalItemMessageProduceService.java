package com.herofactory.kafka.item;


import static com.herofactory.common.Topic.ORIGINAL_TOPIC;

import com.herofactory.kafka.CustomObjectMapper;
import com.herofactory.shop.dto.ItemDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OriginalItemMessageProduceService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CustomObjectMapper objectMapper = new CustomObjectMapper();

    public void sendMessage(ItemDto itemDto) {
        OriginalItemMessage message = convertToMessage(itemDto.getId(), itemDto);

        try {
            kafkaTemplate.send(ORIGINAL_TOPIC, message.getId().toString(), objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private OriginalItemMessage convertToMessage(Long id, ItemDto itemDto) {
        return new OriginalItemMessage(
                id,
                itemDto == null ? null : new OriginalItemMessage.Payload(
                        itemDto.getId(),
                        itemDto.getName(),
                        itemDto.getDescription(),
                        itemDto.getPrice(),
                        itemDto.getUserId(),
                        itemDto.getCategoryName(),
                        itemDto.getImageUrl(),
                        itemDto.getDownloadUrl(),
                        itemDto.getCreatedAt(),
                        itemDto.getUpdatedAt()
                )
        );
    }
}
