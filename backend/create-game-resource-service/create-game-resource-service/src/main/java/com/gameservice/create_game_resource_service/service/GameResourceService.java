package com.gameservice.create_game_resource_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameservice.create_game_resource_service.client.SupabaseClient;
import com.gameservice.create_game_resource_service.exception.TaskNotFoundException;
import com.gameservice.create_game_resource_service.messaging.GameResourceProducer;
import com.gameservice.create_game_resource_service.model.GameResource;
import com.gameservice.create_game_resource_service.model.GameResourceCreationMessage;
import com.gameservice.create_game_resource_service.model.GameResourceCreationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GameResourceService {

    private final SupabaseClient supabaseClient;
    private final ObjectMapper objectMapper;
    private final GameResourceProducer producer;
    private final FastAPIClientService fastAPIClientService;
    private final Map<String, Map<String, Object>> taskStatusMap = new ConcurrentHashMap<>();

    @Value("${task.timeout.seconds:300}")
    private long taskTimeoutSeconds;

    @Autowired
    public GameResourceService(SupabaseClient supabaseClient, ObjectMapper objectMapper,
                               GameResourceProducer producer, FastAPIClientService fastAPIClientService) {
        this.supabaseClient = supabaseClient;
        this.objectMapper = objectMapper;
        this.producer = producer;
        this.fastAPIClientService = fastAPIClientService;
    }

    @Transactional
    public String initiateGameResourceCreation(MultipartFile file, GameResourceCreationRequest request) throws Exception {
        String taskId = UUID.randomUUID().toString();

        GameResourceCreationMessage message = new GameResourceCreationMessage();
        message.setTaskId(taskId);
        message.setFileContent(Base64.getEncoder().encodeToString(file.getBytes()));
        message.setFileName(file.getOriginalFilename());
        message.setRequest(request);

        String jsonMessage = objectMapper.writeValueAsString(message);
        producer.sendMessage(jsonMessage);

        taskStatusMap.put(taskId, Map.of("status", "processing"));

        return taskId;
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        Map<String, Object> status = taskStatusMap.get(taskId);
        if (status == null) {
            throw new TaskNotFoundException(taskId);
        }
        return status;
    }

    public void processTaskUpdate(String taskId, Map<String, Object> status) {
        taskStatusMap.put(taskId, status);
        log.info("Task status updated for taskId: {}, status: {}", taskId, status);
    }

    @Transactional
    public void processGameResourceCreation(GameResourceCreationMessage message) {
        String taskId = message.getTaskId();
        try {
            MultipartFile file = convertToMultipartFile(message);
            Map<String, Object> result = fastAPIClientService.generateImage(file, message.getRequest().getPrompt());
            processTaskUpdate(taskId, result);

            String generationTaskId = (String) result.get("task_id");
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(taskTimeoutSeconds)) {
                Map<String, Object> status = fastAPIClientService.getTaskStatus(generationTaskId);
                processTaskUpdate(taskId, status);

                if ("completed".equals(status.get("status"))) {
                    Resource imageResource = (Resource) status.get("image_data");
                    if (imageResource != null && imageResource.exists()) {
                        GameResource gameResource = createGameResourceFromMessage(message, imageResource);
                        GameResource savedResource = createGameResource(gameResource);
                        processTaskUpdate(taskId, Map.of("status", "completed", "resource", savedResource));
                    } else {
                        processTaskUpdate(taskId, Map.of("status", "failed", "error", "Image resource not found"));
                    }
                    return;
                } else if ("failed".equals(status.get("status"))) {
                    processTaskUpdate(taskId, Map.of("status", "failed", "error", status.get("error")));
                    return;
                }

                Thread.sleep(5000);
            }
            processTaskUpdate(taskId, Map.of("status", "failed", "error", "Task timeout"));
        } catch (Exception e) {
            log.error("Error processing game resource creation for task {}", taskId, e);
            processTaskUpdate(taskId, Map.of("status", "failed", "error", e.getMessage()));
        }
    }

    private GameResource createGameResourceFromMessage(GameResourceCreationMessage message, Resource imageResource) throws IOException {
        GameResource gameResource = new GameResource();
        gameResource.setName(message.getRequest().getName());
        gameResource.setDescription(message.getRequest().getDescription());
        gameResource.setImageUrl("https://example.com/placeholder-image.png"); // 임시 URL 사용
        return gameResource;
    }

    private String uploadImageToSupabase(Resource imageResource) throws IOException {
        // Supabase에 이미지를 업로드하는 로직을 구현해야 합니다.
        // 이 메서드는 업로드된 이미지의 URL을 반환해야 합니다.
        // 이 부분은 Supabase의 스토리지 API를 사용하여 구현해야 합니다.
        return "https://example.com/uploaded-image.png"; // 임시 URL
    }

    public MultipartFile convertToMultipartFile(GameResourceCreationMessage message) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return message.getFileName();
            }

            @Override
            public String getOriginalFilename() {
                return message.getFileName();
            }

            @Override
            public String getContentType() {
                return "image/png";
            }

            @Override
            public boolean isEmpty() {
                return message.getFileContent() == null || message.getFileContent().isEmpty();
            }

            @Override
            public long getSize() {
                return message.getFileContent().length();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Base64.getDecoder().decode(message.getFileContent());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(Base64.getDecoder().decode(message.getFileContent()));
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                throw new UnsupportedOperationException("transferTo is not supported");
            }
        };
    }

    private GameResource createGameResourceFromMessage(GameResourceCreationMessage message, Map<String, Object> status) {
        GameResource gameResource = new GameResource();
        gameResource.setName(message.getRequest().getName());
        gameResource.setDescription(message.getRequest().getDescription());
        gameResource.setImageUrl((String) status.get("image"));
        return gameResource;
    }

    @Transactional
    public GameResource createGameResource(GameResource gameResource) throws IOException {
        try {
            String response = supabaseClient.post("/rest/v1/game_resources", gameResource);
            return objectMapper.readValue(response, GameResource.class);
        } catch (Exception e) {
            log.error("Error creating game resource", e);
            throw e;
        }
    }
}