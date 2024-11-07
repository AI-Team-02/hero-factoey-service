package ai.herofactoryservice.prompt.controller;


import ai.herofactoryservice.common.exception.PromptException;
import ai.herofactoryservice.prompt.dto.prompt.request.PromptRequest;
import ai.herofactoryservice.prompt.dto.prompt.response.PromptResponse;
import ai.herofactoryservice.prompt.dto.common.ApiResponse;
import ai.herofactoryservice.prompt.service.PromptService;
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
        try {
            log.debug("Fetching prompt status for ID: {}", promptId);
            PromptResponse response = promptService.getPromptStatus(promptId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (PromptException e) {
            log.error("Error fetching prompt status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while fetching prompt status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("프롬프트 상태 조회 중 오류가 발생했습니다."));
        }
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