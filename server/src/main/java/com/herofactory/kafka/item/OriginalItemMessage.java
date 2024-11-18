package com.herofactory.kafka.item;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OriginalItemMessage {
    private Long id;
    private Payload payload;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Payload {
        private Long id;
        private String name;
        private String description;
        private int price;
        private Long userId;
        private String categoryName;
        private String imageUrl;
        private String downloadUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
