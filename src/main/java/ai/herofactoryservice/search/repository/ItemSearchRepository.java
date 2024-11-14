package ai.herofactoryservice.search.repository;

import ai.herofactoryservice.search.document.ItemDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ItemSearchRepository extends ElasticsearchRepository<ItemDocument, String> {
}