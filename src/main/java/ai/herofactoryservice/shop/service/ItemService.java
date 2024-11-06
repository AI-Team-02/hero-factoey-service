package ai.herofactoryservice.shop.service;

import ai.herofactoryservice.shop.dto.ItemDto;
import ai.herofactoryservice.shop.dto.ItemsResponse;
import ai.herofactoryservice.shop.entity.Item;
import ai.herofactoryservice.shop.repository.ItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemsResponse getItems(Pageable pageable) {
        Page<Item> itemPage = itemRepository.findAll(pageable);
        List<ItemDto> items = itemPage.getContent().stream()
            .map(ItemDto::from)
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
            .map(ItemDto::from)
            .collect(Collectors.toList());
            
        return new ItemsResponse(
            items,
            !itemPage.isLast(),
            itemPage.getTotalPages(),
            itemPage.getTotalElements()
        );
    }

    public ItemDto getItem(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        return ItemDto.from(item);
    }
}