package ai.herofactoryservice.create_game_resource_service.controller;

import ai.herofactoryservice.create_game_resource_service.model.dto.ApiResponse;
import ai.herofactoryservice.create_game_resource_service.model.dto.PromptRequest;
import ai.herofactoryservice.create_game_resource_service.model.dto.PromptResponse;
import ai.herofactoryservice.create_game_resource_service.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromptResponse>> createPrompt(@RequestBody PromptRequest request) {
        log.debug("Creating prompt with request: {}", request);
        PromptResponse response = promptService.createPrompt(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{promptId}")
    public ResponseEntity<ApiResponse<PromptResponse>> getPromptStatus(@PathVariable String promptId) {
        log.debug("Fetching prompt status for ID: {}", promptId);
        PromptResponse response = promptService.getPromptStatus(promptId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{promptId}/result")
    public ResponseEntity<PromptResponse> getPromptResult(@PathVariable String promptId) {
        log.debug("Fetching prompt result for ID: {}", promptId);
        PromptResponse response = promptService.getPromptStatus(promptId);

        if (response.getStatus().isTerminal()) {
            return ResponseEntity.ok(response);
        } else {
            // 아직 처리 중인 경우 202 Accepted 반환
            return ResponseEntity.accepted().body(response);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Error processing prompt", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("프롬프트 처리 중 오류가 발생했습니다: " + e.getMessage()));
    }
}