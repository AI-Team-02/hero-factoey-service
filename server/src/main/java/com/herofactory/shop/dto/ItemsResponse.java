// ItemListResponse.java
package com.herofactory.shop.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemsResponse {
    private List<ItemDto> items;
    private boolean hasNext;
    private int totalPages;
    private long totalElements;
}