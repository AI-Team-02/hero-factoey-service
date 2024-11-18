package com.herofactory.search.repository;

import com.herofactory.search.document.ItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ItemSearchRepository extends ElasticsearchRepository<ItemDocument, String> {
}