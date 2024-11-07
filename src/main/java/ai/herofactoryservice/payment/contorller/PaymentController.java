package ai.herofactoryservice.payment.contorller;

import ai.herofactoryservice.payment.dto.request.PaymentCancelRequest;
import ai.herofactoryservice.payment.dto.request.PaymentRequest;
import ai.herofactoryservice.payment.dto.response.KakaoPayApproveResponse;
import ai.herofactoryservice.payment.dto.response.KakaoPayCancelResponse;
import ai.herofactoryservice.payment.dto.response.PaymentResponse;
import ai.herofactoryservice.payment.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final KakaoPayService kakaoPayService;

    @PostMapping("/ready")
    public ResponseEntity<PaymentResponse> readyPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("Payment preparation request received: {}", request);
        PaymentResponse response = kakaoPayService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable String paymentId) {
        log.info("Payment status check request received for paymentId: {}", paymentId);
        PaymentResponse response = kakaoPayService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    public ResponseEntity<KakaoPayApproveResponse> approvePayment(
            @RequestParam String pgToken,
            @RequestParam String paymentId) {
        log.info("Payment approval request received. PaymentId: {}, PgToken: {}", paymentId, pgToken);
        KakaoPayApproveResponse response = kakaoPayService.approvePayment(pgToken, paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<KakaoPayCancelResponse> cancelPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentCancelRequest request) {
        log.info("Payment cancellation request received for paymentId: {}", paymentId);
        KakaoPayCancelResponse response = kakaoPayService.cancelPayment(paymentId, request.getCancelReason());
        return ResponseEntity.ok(response);
    }
}