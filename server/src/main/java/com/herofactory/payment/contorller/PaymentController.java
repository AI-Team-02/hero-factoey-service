package com.herofactory.payment.contorller;

import com.herofactory.payment.dto.request.PaymentCancelRequest;
import com.herofactory.payment.dto.request.PaymentRequest;
import com.herofactory.payment.dto.response.KakaoPayApproveResponse;
import com.herofactory.payment.dto.response.KakaoPayCancelResponse;
import com.herofactory.payment.dto.response.PaymentResponse;
import com.herofactory.payment.service.KakaoPayService;
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

@Tag(name = "Payment", description = "결제 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final KakaoPayService kakaoPayService;

    @Operation(
            summary = "결제 준비",
            description = "카카오페이 결제를 위한 준비 요청을 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 준비 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class)
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
    @PostMapping("/ready")
    public ResponseEntity<PaymentResponse> readyPayment(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "결제 준비 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PaymentRequest.class)
                    )
            )
            @RequestBody PaymentRequest request) {
        log.info("Payment preparation request received: {}", request);
        PaymentResponse response = kakaoPayService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 상태 조회",
            description = "결제 ID를 통해 현재 결제 상태를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @Parameter(
                    description = "결제 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String paymentId) {
        log.info("Payment status check request received for paymentId: {}", paymentId);
        PaymentResponse response = kakaoPayService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 승인",
            description = "카카오페이 결제를 승인합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 승인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KakaoPayApproveResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/approve")
    public ResponseEntity<KakaoPayApproveResponse> approvePayment(
            @Parameter(description = "PG사 토큰", required = true)
            @RequestParam String pgToken,
            @Parameter(description = "결제 ID", required = true)
            @RequestParam String paymentId) {
        log.info("Payment approval request received. PaymentId: {}, PgToken: {}", paymentId, pgToken);
        KakaoPayApproveResponse response = kakaoPayService.approvePayment(pgToken, paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 취소",
            description = "결제를 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KakaoPayCancelResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<KakaoPayCancelResponse> cancelPayment(
            @Parameter(
                    description = "결제 ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String paymentId,
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "결제 취소 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PaymentCancelRequest.class)
                    )
            )
            @RequestBody PaymentCancelRequest request) {
        log.info("Payment cancellation request received for paymentId: {}", paymentId);
        KakaoPayCancelResponse response = kakaoPayService.cancelPayment(paymentId, request.getCancelReason());
        return ResponseEntity.ok(response);
    }
}