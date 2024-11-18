package com.herofactory.consumer;

import com.herofactory.common.Topic;
import com.herofactory.inspecteditem.dto.InspectedItemDto;
import com.herofactory.inspecteditem.service.ItemInspectService;
import com.herofactory.kafka.CustomObjectMapper;
import com.herofactory.kafka.inspecteditem.InspectedItemMessageProduceService;
import com.herofactory.kafka.item.OriginalItemMessage;
import com.herofactory.kafka.item.OriginalItemMessageConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AutoInspectionWorker {
    private final CustomObjectMapper objectMapper = new CustomObjectMapper();
    private final ItemInspectService itemInspectService;
    private final InspectedItemMessageProduceService inspectedItemMessageProduceService;

    @KafkaListener(
            topics = {Topic.ORIGINAL_TOPIC},
            groupId = "auto-inspection-consumer-group",
            concurrency = "3"
    )
    public void listen(ConsumerRecord<String, String> message) throws JsonProcessingException {
        OriginalItemMessage originalItemMessage = objectMapper.readValue(message.value(), OriginalItemMessage.class);
        handleCreate(originalItemMessage);
    }

    private void handleCreate(OriginalItemMessage originalItemMessage) {
        InspectedItemDto inspectedItemDto = itemInspectService.inspectAndGetIfValid(
                OriginalItemMessageConverter.toModel(originalItemMessage)
        );
        if (inspectedItemDto == null){
            return;
        }

        inspectedItemMessageProduceService.sendMessage(inspectedItemDto);
    }
}
