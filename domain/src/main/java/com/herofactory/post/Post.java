package com.herofactory.post;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Post {

    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Post update(String title, String content, Long categoryId) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public Post delete() {
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
        this.deletedAt = now;
        return this;
    }


    public static Post generate(
            Long userId,
            String title,
            String content,
            Long categoryId
    ) {
        return new Post(null, title, content, userId, categoryId, LocalDateTime.now(), null, null);
    }
}