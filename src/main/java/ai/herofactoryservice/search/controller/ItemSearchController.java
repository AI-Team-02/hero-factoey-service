package ai.herofactoryservice.search.controller;

import ai.herofactoryservice.search.document.ItemDocument;
import ai.herofactoryservice.search.service.ItemSearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<List<ItemDocument>> searchItems(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        List<ItemDocument> results = itemSearchService.searchItems(keyword);
        return ResponseEntity.ok(results);
    }
}