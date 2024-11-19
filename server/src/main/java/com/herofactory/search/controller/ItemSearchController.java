package com.herofactory.search.controller;

import com.herofactory.search.document.ItemDocument;
import com.herofactory.search.model.ItemInListDto;
import com.herofactory.search.service.ItemSearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/items")
public class ItemSearchController {
    private final ItemSearchService itemSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<ItemInListDto>> searchItems(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam("page") int page
            ) {
        List<ItemDocument> results = itemSearchService.searchItems(keyword, page, 10);
        return ResponseEntity.ok().body(results.stream().map(this::toDto).toList());
    }

    // 인덱스 되는 순간이 지금 다른데? creatAt 이거 대체가 필요해보이는데
    private ItemInListDto toDto(ItemDocument itemDocument) {
        return new ItemInListDto(
                itemDocument.getId(),
                itemDocument.getName(),
                itemDocument.getPrice(),
                itemDocument.getCreatedAt()
        );
    }

}