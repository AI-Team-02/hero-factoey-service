package ai.herofactoryservice.subscription.controller;

import ai.herofactoryservice.subscription.dto.request.SubscriptionCancelRequest;
import ai.herofactoryservice.subscription.dto.request.SubscriptionRequest;
import ai.herofactoryservice.subscription.dto.response.SubscriptionResponse;
import ai.herofactoryservice.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Subscription", description = "구독 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "구독 생성",
            description = "새로운 구독을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 에러"
            )
    })
    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "구독 생성 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SubscriptionRequest.class)
                    )
            )
            @RequestBody SubscriptionRequest request) {
        log.info("Subscription creation request received: {}", request);
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "구독 갱신",
            description = "기존 구독을 갱신합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "구독 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/{subscriptionId}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(
            @Parameter(
                    description = "구독 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String subscriptionId) {
        log.info("Subscription renewal request received for subscriptionId: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.renewSubscription(subscriptionId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "구독 취소",
            description = "구독을 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "구독 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @Parameter(
                    description = "구독 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String subscriptionId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "구독 취소 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SubscriptionCancelRequest.class)
                    )
            )
            @RequestBody SubscriptionCancelRequest request) {
        log.info("Subscription cancellation request received for subscriptionId: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.cancelSubscription(subscriptionId, request.getReason());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "구독 재활성화",
            description = "취소된 구독을 재활성화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 재활성화 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "구독 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/{subscriptionId}/reactivate")
    public ResponseEntity<SubscriptionResponse> reactivateSubscription(
            @Parameter(
                    description = "구독 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String subscriptionId) {
        log.info("Subscription reactivation request received for subscriptionId: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.reactivateSubscription(subscriptionId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원 구독 목록 조회",
            description = "특정 회원의 모든 구독 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<SubscriptionResponse>> getMemberSubscriptions(
            @Parameter(
                    description = "회원 ID",
                    required = true,
                    example = "12345"
            )
            @PathVariable String memberId) {
        log.info("Fetching subscriptions for memberId: {}", memberId);
        List<SubscriptionResponse> subscriptions = subscriptionService.getMemberSubscriptions(memberId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
            summary = "구독 상태 조회",
            description = "구독 ID로 현재 구독 상태를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "구독 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @Parameter(
                    description = "구독 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String subscriptionId) {
        log.info("Fetching subscription details for subscriptionId: {}", subscriptionId);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionStatus(subscriptionId);
        return ResponseEntity.ok(subscription);
    }
}