package com.herofactory.shop.dto;

import com.herofactory.shop.entity.Category;
import com.herofactory.shop.entity.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequest {
    private String name;
    private String description;
    private int price;
    private Long categoryId;
    private Long userId;
    private String imageUrl;
    private String downloadUrl;

    public static Item toEntity(ItemRequest requestDto, Category category) {
        Item item = new Item();
        item.setName(requestDto.getName());
        item.setDescription(requestDto.getDescription());
        item.setPrice(requestDto.getPrice());
        item.setUserId(requestDto.getUserId());
        item.setCategory(category);
        item.setImageUrl(requestDto.getImageUrl());
        item.setDownloadUrl(requestDto.getDownloadUrl());
        return item;
    }
}