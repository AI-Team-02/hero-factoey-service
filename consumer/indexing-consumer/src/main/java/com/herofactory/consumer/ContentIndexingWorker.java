package com.herofactory.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.herofactory.common.Topic;
import com.herofactory.kafka.CustomObjectMapper;
import com.herofactory.kafka.inspecteditem.InspectedItemMessage;
import com.herofactory.search.service.ItemSearchService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ContentIndexingWorker {
    private final CustomObjectMapper objectMapper = new CustomObjectMapper();
    private final ItemSearchService itemSearchService;

    @KafkaListener(
            topics = {Topic.INSPECTED_TOPIC},
            groupId = "indexing-consumer-group",
            concurrency = "3" // partition 3
    )
    public void listen(ConsumerRecord<String, String> message) throws JsonProcessingException {
        InspectedItemMessage inspectedItemMessage = objectMapper.readValue(message.value(), InspectedItemMessage.class);
        handleCreate(inspectedItemMessage);
    }

    private void handleCreate(InspectedItemMessage inspectedItemMessage) {
        itemSearchService.indexItem(inspectedItemMessage.toModel());
    }
}
