package com.herofactory.inspecteditem.dto;

import com.herofactory.shop.dto.ItemDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InspectedItemDto {
    private final ItemDto itemDto;
    private final List<String> autoGeneratedTags;
    private final LocalDateTime inspectedAt;

    public static InspectedItemDto generate(
            ItemDto itemDto,
            List<String> autoGeneratedTags
    ){
        return new InspectedItemDto(
                itemDto,
                autoGeneratedTags,
                LocalDateTime.now()
        );
    }
}
