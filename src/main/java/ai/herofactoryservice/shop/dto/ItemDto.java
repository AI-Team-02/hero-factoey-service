package ai.herofactoryservice.shop.dto;

import ai.herofactoryservice.shop.entity.Item;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private int price;
    private Long categoryId;
//    private String categoryName;
    private String imageUrl;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemDto from(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setCategoryId(item.getCategory().getId());
//        dto.setCategoryName(item.getCategory().getName());
        dto.setImageUrl(item.getImageUrl());
        dto.setDownloadUrl(item.getDownloadUrl());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}