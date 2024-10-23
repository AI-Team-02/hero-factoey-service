package ai.herofactoryservice.create_game_resource_service.service;

import ai.herofactoryservice.create_game_resource_service.exception.PaymentException;
import ai.herofactoryservice.create_game_resource_service.messaging.PaymentProducer;
import ai.herofactoryservice.create_game_resource_service.model.*;
import ai.herofactoryservice.create_game_resource_service.model.dto.*;
import ai.herofactoryservice.create_game_resource_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService {
    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentProducer paymentProducer;

    @Value("${kakao.pay.admin.key}")
    private String adminKey;

    @Value("${kakao.pay.cid}")
    private String cid;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {  // Payment -> PaymentRequest로 변경
        String paymentId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();

        try {
            // 카카오페이 결제 준비 API 호출
            KakaoPayReadyResponse kakaoResponse = preparePayment(paymentId, orderId, request);

            // 결제 정보 저장
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .tid(kakaoResponse.getTid())
                    .shopItemId(request.getShopItemId())
                    .memberId(request.getMemberId())
                    .amount(request.getAmount())
                    .itemName(request.getItemName())
                    .status(PaymentStatus.READY)
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);
            savePaymentLog(payment, "READY", "결제 준비");

            // 메시지 큐로 결제 정보 전송
            PaymentMessage message = PaymentMessage.builder()
                    .paymentId(paymentId)
                    .shopItemId(request.getShopItemId())
                    .memberId(request.getMemberId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.READY)
                    .build();

            paymentProducer.sendPaymentMessage(message);

            return PaymentResponse.builder()
                    .paymentId(paymentId)
                    .status(PaymentStatus.READY)
                    .nextRedirectPcUrl(kakaoResponse.getNextRedirectPcUrl())
                    .nextRedirectMobileUrl(kakaoResponse.getNextRedirectMobileUrl())
                    .tid(kakaoResponse.getTid())
                    .build();

        } catch (Exception e) {
            log.error("결제 준비 중 오류 발생", e);
            throw new PaymentException("결제 준비 중 오류가 발생했습니다.", e);
        }
    }

    private KakaoPayReadyResponse preparePayment(String paymentId, String orderId, PaymentRequest request) {  // Payment -> PaymentRequest로 변경
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + adminKey);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", request.getMemberId().toString());
        params.add("item_name", request.getItemName());
        params.add("quantity", "1");
        params.add("total_amount", request.getAmount().toString());
        params.add("tax_free_amount", "0");
        params.add("approval_url", request.getSuccessUrl());
        params.add("cancel_url", request.getCancelUrl());
        params.add("fail_url", request.getFailUrl());

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        return restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/ready",
                body,
                KakaoPayReadyResponse.class
        );
    }

    @Transactional
    public KakaoPayApproveResponse approvePayment(String pgToken, String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "KakaoAK " + adminKey);
            headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", cid);
            params.add("tid", payment.getTid());
            params.add("partner_order_id", payment.getOrderId());
            params.add("partner_user_id", payment.getMemberId().toString());
            params.add("pg_token", pgToken);

            HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

            KakaoPayApproveResponse response = restTemplate.postForObject(
                    "https://kapi.kakao.com/v1/payment/approve",
                    body,
                    KakaoPayApproveResponse.class
            );

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setApprovedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "APPROVE", "결제 승인");

            return response;

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "FAIL", "결제 승인 실패: " + e.getMessage());
            throw new PaymentException("결제 승인 중 오류가 발생했습니다.", e);
        }
    }

    private void savePaymentLog(Payment payment, String logType, String content) {
        PaymentLog log = PaymentLog.builder()
                .payment(payment)
                .logType(logType)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        paymentLogRepository.save(log);
    }

    // 결제 상태 조회
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .errorMessage(payment.getErrorMessage())
                .build();
    }

    @Transactional
    public void processPayment(PaymentMessage message) {
        Payment payment = paymentRepository.findByPaymentId(message.getPaymentId())
                .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

        try {
            // 결제 상태 검증
            if (payment.getStatus() != PaymentStatus.READY) {
                throw new PaymentException("유효하지 않은 결제 상태입니다.");
            }

            // 결제 정보 검증
            validatePaymentDetails(payment, message);

            // 결제 처리 상태 업데이트
            payment.setStatus(PaymentStatus.IN_PROGRESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "PROCESS", "결제 처리 시작");

            // 여기에 실제 결제 처리 로직 추가
            // 예: 외부 결제 시스템 연동, 재고 확인 등

            // 처리 완료 후 상태 업데이트
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "COMPLETE", "결제 처리 완료");

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "FAIL", "결제 처리 실패: " + e.getMessage());
            throw new PaymentException("결제 처리 중 오류가 발생했습니다.", e);
        }
    }

    private void validatePaymentDetails(Payment payment, PaymentMessage message) {
        if (!payment.getAmount().equals(message.getAmount())) {
            throw new PaymentException("결제 금액이 일치하지 않습니다.");
        }
        if (!payment.getShopItemId().equals(message.getShopItemId())) {
            throw new PaymentException("상품 정보가 일치하지 않습니다.");
        }
        if (!payment.getMemberId().equals(message.getMemberId())) {
            throw new PaymentException("회원 정보가 일치하지 않습니다.");
        }
    }
    @Transactional
    public KakaoPayCancelResponse cancelPayment(String paymentId, String cancelReason) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("완료된 결제만 취소할 수 있습니다.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "KakaoAK " + adminKey);
            headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", cid);
            params.add("tid", payment.getTid());
            params.add("cancel_amount", payment.getAmount().toString());
            params.add("cancel_tax_free_amount", "0");
            params.add("cancel_reason", cancelReason);

            HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

            KakaoPayCancelResponse response = restTemplate.postForObject(
                    "https://kapi.kakao.com/v1/payment/cancel",
                    body,
                    KakaoPayCancelResponse.class
            );

            // 결제 정보 업데이트
            payment.setStatus(PaymentStatus.CANCELED);
            payment.setCancelAmount(payment.getAmount());
            payment.setCancelReason(cancelReason);
            payment.setCanceledAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // 로그 저장
            savePaymentLog(payment, "CANCEL", "결제 취소: " + cancelReason);

            return response;

        } catch (Exception e) {
            savePaymentLog(payment, "CANCEL_FAIL", "결제 취소 실패: " + e.getMessage());
            throw new PaymentException("결제 취소 중 오류가 발생했습니다.", e);
        }
    }
}