package com.herofactory.kafka.item;

import com.herofactory.shop.dto.ItemDto;

public class OriginalItemMessageConverter {
    public static ItemDto toModel(OriginalItemMessage originalItemMessage) {
        return new ItemDto(
                originalItemMessage.getPayload().getId(),
                originalItemMessage.getPayload().getName(),
                originalItemMessage.getPayload().getDescription(),
                originalItemMessage.getPayload().getPrice(),
                originalItemMessage.getPayload().getCategoryName(),
                originalItemMessage.getPayload().getUserId(),
                originalItemMessage.getPayload().getImageUrl(),
                originalItemMessage.getPayload().getDownloadUrl(),
                originalItemMessage.getPayload().getCreatedAt(),
                originalItemMessage.getPayload().getUpdatedAt()
        );
    }
}
