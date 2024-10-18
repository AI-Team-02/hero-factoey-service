package com.gameservice.create_game_resource_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameservice.create_game_resource_service.model.GameResourceCreationMessage;
import com.gameservice.create_game_resource_service.model.GameResourceCreationRequest;
import com.gameservice.create_game_resource_service.service.GameResourceService;
//import com.gameservice.create_game_resource_service.service.RabbitMQProducer;
import com.gameservice.create_game_resource_service.exception.TaskNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:8080")
@Slf4j
@RestController
@RequestMapping("/api")
public class GameResourceController {

    private final GameResourceService gameResourceService;

    @Autowired
    public GameResourceController(GameResourceService gameResourceService) {
        this.gameResourceService = gameResourceService;
    }

    @PostMapping("/generate/")
    public ResponseEntity<?> generateGameResource(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") String requestJSON) {
        try {
            log.info("이미지 생성 요청 수신: {}", requestJSON);

            ObjectMapper objectMapper = new ObjectMapper();
            GameResourceCreationRequest request = objectMapper.readValue(requestJSON, GameResourceCreationRequest.class);

            String taskId = gameResourceService.initiateGameResourceCreation(file, request);

            return ResponseEntity.accepted().body(Map.of("task_id", taskId, "status", "processing"));
        } catch (Exception e) {
            log.error("게임 리소스 생성 요청 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            Map<String, Object> status = gameResourceService.getTaskStatus(taskId);
            return ResponseEntity.ok(status);
        } catch (TaskNotFoundException e) {
            log.error("Task not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("작업 상태 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTaskNotFoundException(TaskNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}