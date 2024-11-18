package com.herofactory.shop.dto;

import com.herofactory.shop.entity.Item;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private int price;
    private String categoryName;
    private Long userId;
    private String imageUrl;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemDto createDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setCategoryName(item.getCategory().getName());
        dto.setUserId(item.getUserId());
        dto.setImageUrl(item.getImageUrl());
        dto.setDownloadUrl(item.getDownloadUrl());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}