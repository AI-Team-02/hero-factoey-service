package com.herofactory.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostUpdateRequest {
    private String title;
    private String content;
    private Long categoryId;
}