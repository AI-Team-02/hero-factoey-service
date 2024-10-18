package com.gameservice.create_game_resource_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
public class FastAPIClientService {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    // comfy.output.dir 속성 제거

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FastAPIClientService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> generateImage(MultipartFile file, String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("request", objectMapper.writeValueAsString(Map.of("prompt", prompt)));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl + "/generate/", requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) {
            log.info("Image generation request accepted. Task ID: {}", response.getBody().get("task_id"));
            return response.getBody();
        } else {
            log.error("Failed to generate image. Status code: {}, Response: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to generate image: " + response.getBody());
        }
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        ResponseEntity<Map> response = restTemplate.getForEntity(fastApiUrl + "/status/" + taskId, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Retrieved status for task: {}. Status: {}", taskId, response.getBody().get("status"));
            return response.getBody();
        } else {
            log.error("Failed to get task status. Task ID: {}, Status code: {}, Response: {}", taskId, response.getStatusCode(), response.getBody());
            throw new RuntimeException("Failed to get task status: " + response.getBody());
        }
    }
}