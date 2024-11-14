package ai.herofactoryservice.shop.dto;

import ai.herofactoryservice.shop.entity.Category;
import ai.herofactoryservice.shop.entity.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestDto {
    private String name;
    private String description;
    private int price;
    private Long categoryId;
    private String imageUrl;
    private String downloadUrl;

    public static Item toEntity(ItemRequestDto requestDto, Category category) {
        Item item = new Item();
        item.setName(requestDto.getName());
        item.setDescription(requestDto.getDescription());
        item.setPrice(requestDto.getPrice());
        item.setCategory(category);
        item.setImageUrl(requestDto.getImageUrl());
        item.setDownloadUrl(requestDto.getDownloadUrl());
        return item;
    }
}