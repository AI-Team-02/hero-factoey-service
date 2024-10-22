package com.gameservice.create_game_resource_service.controller;

import com.gameservice.create_game_resource_service.model.Member;
import com.gameservice.create_game_resource_service.service.GameResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Game Resource Generation", description = "Game Resource Generation APIs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GameResourceController {
    private final GameResourceService gameResourceService;

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Generate game resource",
            description = "Upload an image file and provide a prompt to generate a game resource")
    public ResponseEntity<?> generateGameResource(
            @Parameter(description = "Image file") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Prompt for image generation") @RequestPart("prompt") String prompt,
            Authentication authentication) {
        try {
            log.info("Received generate request. Prompt: {}", prompt);
            log.info("File received: {}, size: {}", file.getOriginalFilename(), file.getSize());

            Member member = (Member) authentication.getPrincipal();
            String taskId = gameResourceService.initiateGameResourceCreation(file, prompt, member);

            return ResponseEntity.accepted().body(Map.of(
                    "task_id", taskId,
                    "status", "processing",
                    "user_id", member.getId()
            ));
        } catch (Exception e) {
            log.error("Error processing generate request", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{taskId}")
    @Operation(summary = "Get task status",
            description = "Retrieve the status of a game resource generation task")
    public ResponseEntity<?> getTaskStatus(
            @Parameter(description = "Task ID") @PathVariable String taskId,
            Authentication authentication) {
        try {
            Member member = (Member) authentication.getPrincipal();
            Map<String, Object> status = gameResourceService.getTaskStatus(taskId, member.getId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error retrieving task status", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/resources")
    @Operation(summary = "Get user resources",
            description = "Retrieve all resources created by the current user")
    public ResponseEntity<?> getUserResources(Authentication authentication) {
        try {
            Member member = (Member) authentication.getPrincipal();
            return ResponseEntity.ok(gameResourceService.getUserResources(member.getId()));
        } catch (Exception e) {
            log.error("Error retrieving user resources", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}