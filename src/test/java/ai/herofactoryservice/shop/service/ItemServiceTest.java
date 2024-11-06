package ai.herofactoryservice.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import ai.herofactoryservice.shop.dto.ItemsResponse;
import ai.herofactoryservice.shop.entity.Category;
import ai.herofactoryservice.shop.entity.Item;
import ai.herofactoryservice.shop.repository.CategoryRepository;
import ai.herofactoryservice.shop.repository.ItemRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@SpringBootTest
@Transactional
class ItemServiceTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        Category weapons = new Category();
        weapons.setName("무기");
        categoryRepository.save(weapons);

        Category armor = new Category();
        armor.setName("방어구");
        categoryRepository.save(armor);

        List<Item> items = new ArrayList<>();

        // 무기 아이템 (10개)
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

    @Test
    @DisplayName("아이템 목록 페이징 조회")
    void getItems() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        // when
        ItemsResponse response = itemService.getItems(pageRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(20);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getItems().size()).isEqualTo(10);
    }

}