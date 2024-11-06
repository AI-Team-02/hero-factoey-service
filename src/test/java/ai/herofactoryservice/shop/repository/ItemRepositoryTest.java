package ai.herofactoryservice.shop.repository;


import static org.assertj.core.api.Assertions.assertThat;

import ai.herofactoryservice.shop.entity.Category;
import ai.herofactoryservice.shop.entity.Item;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        category1 = new Category();
        category1.setName("Category 1");
        entityManager.persist(category1);

        category2 = new Category();
        category2.setName("Category 2");
        entityManager.persist(category2);

        // 테스트용 아이템 생성
        for (int i = 1; i <= 15; i++) {
            Item item = new Item();
            item.setName("Item " + i);
            item.setDescription("Description " + i);
            item.setPrice(1000 * i);
            item.setCategory(i % 2 == 0 ? category1 : category2);
            item.setImageUrl("http://example.com/image" + i + ".jpg");
            item.setDownloadUrl("http://example.com/download" + i + ".zip");
            entityManager.persist(item);
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("전체 아이템을 페이징하여 조회한다")
    void findAll() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<Item> page = itemRepository.findAll(pageRequest);

        // then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("특정 카테고리의 아이템들만 조회한다")
    void findByCategoryId() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Item> page = itemRepository.findByCategoryId(category1.getId(), pageRequest);

        // then
        assertThat(page.getContent())
                .isNotEmpty()
                .allMatch(item -> item.getCategory().getId().equals(category1.getId()));
    }

    @Test
    @DisplayName("단일 아이템을 ID로 조회한다")
    void findById() {
        // given
        Item item = itemRepository.findAll().get(0);
        entityManager.clear();

        // when
        Item foundItem = itemRepository.findById(item.getId()).orElse(null);

        // then
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getId()).isEqualTo(item.getId());
    }
}