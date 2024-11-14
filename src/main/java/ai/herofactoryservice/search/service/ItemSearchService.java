package ai.herofactoryservice.search.service;

import ai.herofactoryservice.search.document.ItemDocument;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemSearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    public List<ItemDocument> searchItems(String keyword) {
            Query query = QueryBuilders.bool()
                    .should(
                            QueryBuilders.match()
                                    .field("name")
                                    .query(keyword)
                                    .build()._toQuery(),
                            QueryBuilders.match()
                                    .field("description")
                                    .query(keyword)
                                    .build()._toQuery(),
                            QueryBuilders.term()
                                    .field("categoryName")
                                    .value(keyword)
                                    .build()._toQuery()
                    )
                    .minimumShouldMatch("1")
                    .build()._toQuery();

            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(query)
                    .build();

            return elasticsearchOperations.search(searchQuery, ItemDocument.class)
                    .getSearchHits()
                    .stream()
                    .map(SearchHit::getContent)
                    .filter(Objects::nonNull)
                    .toList();
    }
}