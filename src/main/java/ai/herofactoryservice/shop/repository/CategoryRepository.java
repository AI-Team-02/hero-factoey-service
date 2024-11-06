package ai.herofactoryservice.shop.repository;

import ai.herofactoryservice.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
