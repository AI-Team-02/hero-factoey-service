package com.herofactory.common.logging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.herofactory.common.logging.model.ApiAccessLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendLog(ApiAccessLog logData) {
        try {
            String jsonLog = objectMapper.writeValueAsString(logData);
            kafkaTemplate.send("api-access-log", jsonLog)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send log to Kafka", ex);
                    }
                });
        } catch (JsonProcessingException e) {
            log.error("Error converting log to JSON", e);
        }
    }
}
