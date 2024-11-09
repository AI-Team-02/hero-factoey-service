package ai.herofactoryservice.prompt.controller;

import ai.herofactoryservice.common.exception.PromptException;
import ai.herofactoryservice.prompt.dto.request.PromptRequest;
import ai.herofactoryservice.prompt.dto.response.PromptResponse;
import ai.herofactoryservice.common.dto.CommonResponse;
import ai.herofactoryservice.prompt.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Prompt", description = "프롬프트 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PromptController {

    private final PromptService promptService;

    @Operation(
            summary = "프롬프트 생성",
            description = "새로운 프롬프트를 생성하고 처리를 시작합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프롬프트 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CommonResponse.class,
                                    subTypes = {PromptResponse.class}
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 에러",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CommonResponse<PromptResponse>> createPrompt(
            @RequestBody(
                    description = "프롬프트 생성 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PromptRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody PromptRequest request
    ) {
        log.debug("Creating prompt with request: {}", request);
        PromptResponse response = promptService.createPrompt(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
            summary = "프롬프트 상태 조회",
            description = "프롬프트 ID로 현재 상태를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CommonResponse.class,
                                    subTypes = {PromptResponse.class}
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프롬프트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class)
                    )
            )
    })
    @GetMapping("/{promptId}")
    public ResponseEntity<CommonResponse<PromptResponse>> getPromptStatus(
            @Parameter(
                    description = "프롬프트 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String promptId
    ) {
        try {
            log.debug("Fetching prompt status for ID: {}", promptId);
            PromptResponse response = promptService.getPromptStatus(promptId);
            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (PromptException e) {
            log.error("Error fetching prompt status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CommonResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while fetching prompt status", e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("프롬프트 상태 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "프롬프트 결과 조회",
            description = "프롬프트 처리 결과를 조회합니다. 처리가 완료되지 않은 경우 202 상태 코드를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처리 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromptResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "202",
                    description = "처리 중",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromptResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프롬프트를 찾을 수 없음"
            )
    })
    @GetMapping("/{promptId}/result")
    public ResponseEntity<PromptResponse> getPromptResult(
            @Parameter(
                    description = "프롬프트 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String promptId
    ) {
        log.debug("Fetching prompt result for ID: {}", promptId);
        PromptResponse response = promptService.getPromptStatus(promptId);

        if (response.getStatus().isTerminal()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.accepted().body(response);
        }
    }
}