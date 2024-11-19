package com.herofactory.search.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemInListDto {
    private final Long id;
    private final String name;
    private final long price;
    private final LocalDateTime createdAt;
}