package com.herofactory.shop.service;

import com.herofactory.kafka.item.OriginalItemMessageProduceService;
import com.herofactory.search.document.ItemDocument;
import com.herofactory.search.service.ItemSearchService;
import com.herofactory.shop.dto.ItemDto;
import com.herofactory.shop.dto.ItemRequest;
import com.herofactory.shop.dto.ItemsResponse;
import com.herofactory.shop.entity.Category;
import com.herofactory.shop.entity.Item;
import com.herofactory.shop.repository.CategoryRepository;
import com.herofactory.shop.repository.ItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemSearchService itemSearchService;
    private final OriginalItemMessageProduceService originalItemMessageProduceService;

    @Transactional
    public ItemDto saveItem(ItemRequest requestDto) {
        // 카테고리 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Item item = itemRepository.save(ItemRequest.toEntity(requestDto, category));
        ItemDto itemDto = ItemDto.createDto(item);
//        itemSearchService.save(toDocument(itemDto));

        originalItemMessageProduceService.sendMessage(itemDto);
        return itemDto;
    }

    public ItemsResponse getItems(Pageable pageable) {
        Page<Item> itemPage = itemRepository.findAll(pageable);
        List<ItemDto> items = itemPage.getContent().stream()
                .map(ItemDto::createDto)
                .collect(Collectors.toList());

        return new ItemsResponse(
                items,
                !itemPage.isLast(),
                itemPage.getTotalPages(),
                itemPage.getTotalElements()
        );
    }

    public ItemsResponse getItemsByCategory(Long categoryId, Pageable pageable) {
        Page<Item> itemPage = itemRepository.findByCategoryId(categoryId, pageable);
        List<ItemDto> items = itemPage.getContent().stream()
                .map(ItemDto::createDto)
                .collect(Collectors.toList());

        return new ItemsResponse(
                items,
                !itemPage.isLast(),
                itemPage.getTotalPages(),
                itemPage.getTotalElements()
        );
    }

    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        return ItemDto.createDto(item);
    }

    public ItemsResponse searchItems(String keyword, Pageable pageable) {
        Page<Item> itemPage;

        if (StringUtils.hasText(keyword)) {
            itemPage = itemRepository.findByNameContaining(keyword, pageable);
        } else {
            itemPage = itemRepository.findAll(pageable);
        }

        List<ItemDto> items = itemPage.getContent().stream()
                .map(ItemDto::createDto)
                .collect(Collectors.toList());

        return new ItemsResponse(
                items,
                !itemPage.isLast(),
                itemPage.getTotalPages(),
                itemPage.getTotalElements()
        );
    }

    private ItemDocument toDocument(ItemDto itemDto) {
        ItemDocument document = new ItemDocument();
        document.setId(itemDto.getId());
        document.setName(itemDto.getName());
        document.setDescription(itemDto.getDescription());
        document.setPrice(itemDto.getPrice());
        document.setCategoryName(itemDto.getCategoryName());
        document.setIndexAt(LocalDateTime.now());
        return document;
    }
}
