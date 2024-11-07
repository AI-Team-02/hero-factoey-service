package ai.herofactoryservice.shop.controller;

import ai.herofactoryservice.shop.dto.ItemDto;
import ai.herofactoryservice.shop.dto.ItemsResponse;
import ai.herofactoryservice.shop.entity.Category;
import ai.herofactoryservice.shop.entity.Item;
import ai.herofactoryservice.shop.repository.CategoryRepository;
import ai.herofactoryservice.shop.repository.ItemRepository;
import ai.herofactoryservice.shop.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Shop", description = "상점 아이템 조회 & 검색 API")
public class ItemController {
    private final ItemService itemService;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    @Operation(summary = "전체 아이템 목록 조회(무한 스크롤)", description = "페이징 처리된 전체 아이템 목록을 조회합니다")
    @GetMapping("/items")
    public ResponseEntity<ItemsResponse> getItems(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(itemService.getItems(pageRequest));
    }

    @Operation(summary = "카테고리별 아이템 목록 조회(무한 스크롤)", description = "특정 카테고리의 아이템 목록을 조회합니다")
    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<ItemsResponse> getItemsByCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(itemService.getItemsByCategory(categoryId, pageRequest));
    }

    @Operation(summary = "단일 아이템 조회", description = "특정 ID의 아이템 상세 정보를 조회합니다")
    @GetMapping("/items/{id}")
    public ResponseEntity<ItemDto> getItem(
            @Parameter(description = "아이템 ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @Operation(summary = "아이템 검색(무한 스크롤 + Debounce)", description = "아이템을 이름으로 검색합니다.")
    @GetMapping("/items/search")
    public ResponseEntity<ItemsResponse> searchItems(
            @Parameter(description = "검색 키워드(상품명)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(itemService.searchItems(keyword, pageRequest));
    }

    //    @PostConstruct
    public void init() {
        // 카테고리 생성
        Category weapons = new Category();
        weapons.setName("무기");
        categoryRepository.save(weapons);

        Category armor = new Category();
        armor.setName("방어구");
        categoryRepository.save(armor);

        Category potions = new Category();
        potions.setName("물약");
        categoryRepository.save(potions);

        List<Item> items = new ArrayList<>();

        // 무기 아이템 (12개)
        items.add(createItem("엑스칼리버", "전설의 성검", 1000000, weapons,
                "https://example.com/images/excalibur.jpg", "https://example.com/downloads/excalibur"));
        items.add(createItem("미스릴 대검", "미스릴로 제작된 강력한 대검", 50000, weapons,
                "https://example.com/images/mithril-sword.jpg", "https://example.com/downloads/mithril-sword"));
        items.add(createItem("불의 검", "화염의 기운이 깃든 마검", 75000, weapons,
                "https://example.com/images/fire-sword.jpg", "https://example.com/downloads/fire-sword"));
        items.add(createItem("얼음 창", "영구 동결의 힘을 가진 창", 70000, weapons,
                "https://example.com/images/ice-spear.jpg", "https://example.com/downloads/ice-spear"));
        items.add(createItem("번개 단검", "번개의 속도를 가진 단검", 45000, weapons,
                "https://example.com/images/lightning-dagger.jpg", "https://example.com/downloads/lightning-dagger"));
        items.add(createItem("드래곤 슬레이어", "용을 처치하기 위해 제작된 대검", 120000, weapons,
                "https://example.com/images/dragon-slayer.jpg", "https://example.com/downloads/dragon-slayer"));
        items.add(createItem("암흑검", "어둠의 기운이 깃든 검", 80000, weapons,
                "https://example.com/images/dark-sword.jpg", "https://example.com/downloads/dark-sword"));
        items.add(createItem("성스러운 창", "신성한 기운이 깃든 창", 90000, weapons,
                "https://example.com/images/holy-spear.jpg", "https://example.com/downloads/holy-spear"));
        items.add(createItem("바람의 활", "바람을 다루는 신비한 활", 85000, weapons,
                "https://example.com/images/wind-bow.jpg", "https://example.com/downloads/wind-bow"));
        items.add(createItem("대지의 도끼", "대지의 힘이 깃든 도끼", 65000, weapons,
                "https://example.com/images/earth-axe.jpg", "https://example.com/downloads/earth-axe"));
        items.add(createItem("혼돈의 지팡이", "혼돈의 마력이 깃든 지팡이", 95000, weapons,
                "https://example.com/images/chaos-staff.jpg", "https://example.com/downloads/chaos-staff"));
        items.add(createItem("정의의 망치", "정의의 심판을 내리는 망치", 70000, weapons,
                "https://example.com/images/justice-hammer.jpg", "https://example.com/downloads/justice-hammer"));

        // 방어구 아이템 (10개)
        items.add(createItem("미스릴 갑옷", "미스릴로 제작된 튼튼한 갑옷", 80000, armor,
                "https://example.com/images/mithril-armor.jpg", "https://example.com/downloads/mithril-armor"));
        items.add(createItem("용의 비늘 갑옷", "용의 비늘로 만든 최상급 갑옷", 150000, armor,
                "https://example.com/images/dragon-armor.jpg", "https://example.com/downloads/dragon-armor"));
        items.add(createItem("빛나는 판금갑옷", "성스러운 기운이 깃든 갑옷", 70000, armor,
                "https://example.com/images/holy-armor.jpg", "https://example.com/downloads/holy-armor"));
        items.add(createItem("그림자 로브", "그림자 속성이 깃든 로브", 45000, armor,
                "https://example.com/images/shadow-robe.jpg", "https://example.com/downloads/shadow-robe"));
        items.add(createItem("불사조의 망토", "불사조의 깃털로 만든 망토", 100000, armor,
                "https://example.com/images/phoenix-cloak.jpg", "https://example.com/downloads/phoenix-cloak"));
        items.add(createItem("얼음 방패", "영구 빙결의 방패", 60000, armor,
                "https://example.com/images/ice-shield.jpg", "https://example.com/downloads/ice-shield"));
        items.add(createItem("마법 부츠", "마법 강화 부츠", 35000, armor,
                "https://example.com/images/magic-boots.jpg", "https://example.com/downloads/magic-boots"));
        items.add(createItem("바람의 건틀렛", "바람의 가호가 깃든 건틀렛", 40000, armor,
                "https://example.com/images/wind-gauntlet.jpg", "https://example.com/downloads/wind-gauntlet"));
        items.add(createItem("대지의 투구", "대지의 축복이 깃든 투구", 55000, armor,
                "https://example.com/images/earth-helmet.jpg", "https://example.com/downloads/earth-helmet"));
        items.add(createItem("화염의 팔찌", "화염 저항 팔찌", 30000, armor,
                "https://example.com/images/fire-bracelet.jpg", "https://example.com/downloads/fire-bracelet"));

        // 물약 아이템 (8개)
        items.add(createItem("힘의 물약", "일시적으로 힘을 증가시키는 물약", 1000, potions,
                "https://example.com/images/strength-potion.jpg", "https://example.com/downloads/strength-potion"));
        items.add(createItem("회복 물약", "체력을 회복시키는 물약", 500, potions,
                "https://example.com/images/healing-potion.jpg", "https://example.com/downloads/healing-potion"));
        items.add(createItem("마나 물약", "마나를 회복시키는 물약", 800, potions,
                "https://example.com/images/mana-potion.jpg", "https://example.com/downloads/mana-potion"));
        items.add(createItem("속도 증가 물약", "이동속도를 증가시키는 물약", 1200, potions,
                "https://example.com/images/speed-potion.jpg", "https://example.com/downloads/speed-potion"));
        items.add(createItem("저항의 물약", "모든 속성 저항력을 증가시키는 물약", 1500, potions,
                "https://example.com/images/resistance-potion.jpg", "https://example.com/downloads/resistance-potion"));
        items.add(createItem("투명 물약", "일시적으로 투명해지는 물약", 2000, potions,
                "https://example.com/images/invisibility-potion.jpg",
                "https://example.com/downloads/invisibility-potion"));
        items.add(createItem("지혜의 물약", "경험치 획득량을 증가시키는 물약", 1800, potions,
                "https://example.com/images/wisdom-potion.jpg", "https://example.com/downloads/wisdom-potion"));
        items.add(createItem("행운의 물약", "아이템 드롭률을 증가시키는 물약", 2500, potions,
                "https://example.com/images/luck-potion.jpg", "https://example.com/downloads/luck-potion"));

        itemRepository.saveAll(items);
    }

    private Item createItem(String name, String description, int price,
                            Category category, String imageUrl, String downloadUrl) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        item.setDownloadUrl(downloadUrl);
        return item;
    }
}