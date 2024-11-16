package com.herofactory.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PostDto {
    private final Long id;
    private final String title;
    private final String content;
    private final Long userId;
    private final Long categoryId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
}