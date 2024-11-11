package ai.herofactoryservice.payment.service;

import ai.herofactoryservice.common.exception.PaymentException;
import ai.herofactoryservice.payment.dto.request.PaymentRequest;
import ai.herofactoryservice.payment.dto.response.KakaoPayApproveResponse;
import ai.herofactoryservice.payment.dto.response.KakaoPayCancelResponse;
import ai.herofactoryservice.payment.dto.response.KakaoPayReadyResponse;
import ai.herofactoryservice.payment.dto.response.PaymentResponse;
import ai.herofactoryservice.payment.entity.Payment;
import ai.herofactoryservice.payment.entity.PaymentLog;
import ai.herofactoryservice.payment.entity.enums.PaymentStatus;
import ai.herofactoryservice.payment.infrastructure.messaging.producer.PaymentProducer;
import ai.herofactoryservice.payment.repository.*;
import ai.herofactoryservice.payment.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
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
    private final PlatformTransactionManager transactionManager;

    @Value("${kakao.pay.admin.key}")
    private String adminKey;

    @Value("${kakao.pay.cid}")
    private String cid;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PaymentResponse initiatePayment(PaymentRequest request) {
        String paymentId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            KakaoPayReadyResponse kakaoResponse = preparePayment(paymentId, orderId, request);

            Payment payment = createPayment(paymentId, orderId, request, kakaoResponse);
            paymentRepository.save(payment);

            savePaymentLog(payment, "READY", "결제 준비");

            PaymentMessage message = createPaymentMessage(payment);
            paymentProducer.sendPaymentMessage(message);

            transactionManager.commit(status);

            return createPaymentResponse(payment, kakaoResponse);

        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("결제 준비 중 오류 발생", e);
            throw new PaymentException("결제 준비 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public KakaoPayApproveResponse approvePayment(String pgToken, String paymentId) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Payment payment = paymentRepository.findByPaymentIdWithLock(paymentId)
                    .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            validatePaymentStatus(payment);

            KakaoPayApproveResponse response = processKakaoPayApproval(payment, pgToken);

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setApprovedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "APPROVE", "결제 승인");

            PaymentMessage message = createPaymentMessage(payment);
            paymentProducer.sendPaymentMessage(message);

            transactionManager.commit(status);
            return response;

        } catch (Exception e) {
            transactionManager.rollback(status);
            handlePaymentApprovalError(paymentId, e);
            throw new PaymentException("결제 승인 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public KakaoPayCancelResponse cancelPayment(String paymentId, String cancelReason) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Payment payment = paymentRepository.findByPaymentIdWithLock(paymentId)
                    .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            validateCancellablePayment(payment);

            KakaoPayCancelResponse response = processKakaoPayCancel(payment, cancelReason);

            payment.setStatus(PaymentStatus.CANCELED);
            payment.setCancelAmount(payment.getAmount());
            payment.setCancelReason(cancelReason);
            payment.setCanceledAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "CANCEL", "결제 취소: " + cancelReason);

            PaymentMessage message = createCancellationMessage(payment);
            paymentProducer.sendPaymentMessage(message);

            transactionManager.commit(status);
            return response;

        } catch (Exception e) {
            transactionManager.rollback(status);
            handlePaymentCancellationError(paymentId, e);
            throw new PaymentException("결제 취소 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void processPayment(PaymentMessage message) {
        Payment payment = paymentRepository.findByPaymentIdWithLock(message.getPaymentId())
                .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

        try {
            validatePaymentDetails(payment, message);

            payment.setStatus(PaymentStatus.IN_PROGRESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "PROCESS", "결제 처리 시작");

            processPaymentLogic(payment);

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setApprovedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "COMPLETE", "결제 처리 완료");

        } catch (Exception e) {
            handlePaymentProcessError(payment.getPaymentId(), e);
            throw new PaymentException("결제 처리 중 오류가 발생했습니다.", e);
        }
    }

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

    // Private helper methods
    private Payment createPayment(String paymentId, String orderId, PaymentRequest request,
                                  KakaoPayReadyResponse kakaoResponse) {
        return Payment.builder()
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
    }

    private PaymentMessage createPaymentMessage(Payment payment) {
        return PaymentMessage.builder()
                .paymentId(payment.getPaymentId())
                .shopItemId(payment.getShopItemId())
                .memberId(payment.getMemberId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();
    }

    private PaymentMessage createCancellationMessage(Payment payment) {
        return PaymentMessage.builder()
                .paymentId(payment.getPaymentId())
                .shopItemId(payment.getShopItemId())
                .memberId(payment.getMemberId())
                .amount(payment.getAmount())
                .status(PaymentStatus.CANCELED)
                .build();
    }

    private PaymentResponse createPaymentResponse(Payment payment, KakaoPayReadyResponse kakaoResponse) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .nextRedirectPcUrl(kakaoResponse.getNextRedirectPcUrl())
                .nextRedirectMobileUrl(kakaoResponse.getNextRedirectMobileUrl())
                .tid(kakaoResponse.getTid())
                .build();
    }

    private void validatePaymentDetails(Payment payment, PaymentMessage message) {
        if (!payment.getAmount().equals(message.getAmount())) {
            log.error("Payment amount mismatch - Payment: {}, Message: {}",
                    payment.getAmount(), message.getAmount());
            throw new PaymentException("결제 금액이 일치하지 않습니다.");
        }
        if (!payment.getShopItemId().equals(message.getShopItemId())) {
            log.error("Shop item ID mismatch - Payment: {}, Message: {}",
                    payment.getShopItemId(), message.getShopItemId());
            throw new PaymentException("상품 정보가 일치하지 않습니다.");
        }
        if (!payment.getMemberId().equals(message.getMemberId())) {
            log.error("Member ID mismatch - Payment: {}, Message: {}",
                    payment.getMemberId(), message.getMemberId());
            throw new PaymentException("회원 정보가 일치하지 않습니다.");
        }
    }

    private void validatePaymentStatus(Payment payment) {
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new PaymentException("유효하지 않은 결제 상태입니다.");
        }
    }

    private void validateCancellablePayment(Payment payment) {
        if (!payment.getStatus().canCancel()) {
            throw new PaymentException("취소할 수 없는 결제 상태입니다.");
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

    private void processPaymentLogic(Payment payment) {
        log.info("Processing payment: {}", payment.getPaymentId());
    }

    private void handlePaymentProcessError(String paymentId, Exception e) {
        try {
            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "FAIL", "결제 처리 실패: " + e.getMessage());
        } catch (Exception ex) {
            log.error("결제 에러 처리 중 추가 오류 발생", ex);
        }
    }

    private void handlePaymentApprovalError(String paymentId, Exception e) {
        try {
            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "FAIL", "결제 승인 실패: " + e.getMessage());
        } catch (Exception ex) {
            log.error("결제 승인 에러 처리 중 추가 오류 발생", ex);
        }
    }

    private void handlePaymentCancellationError(String paymentId, Exception e) {
        try {
            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            savePaymentLog(payment, "FAIL", "결제 취소 실패: " + e.getMessage());
        } catch (Exception ex) {
            log.error("결제 취소 에러 처리 중 추가 오류 발생", ex);
        }
    }

    private KakaoPayReadyResponse preparePayment(String paymentId, String orderId, PaymentRequest request) {
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

    private KakaoPayApproveResponse processKakaoPayApproval(Payment payment, String pgToken) {
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

        return restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/approve",
                body,
                KakaoPayApproveResponse.class
        );
    }

    private KakaoPayCancelResponse processKakaoPayCancel(Payment payment, String cancelReason) {
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

        return restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/cancel",
                body,
                KakaoPayCancelResponse.class
        );
    }
}