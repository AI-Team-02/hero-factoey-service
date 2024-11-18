package com.herofactory.subscription.service;

import com.herofactory.common.exception.PlanNotFoundException;
import com.herofactory.common.exception.SubscriptionException;
import com.herofactory.payment.dto.request.PaymentRequest;
import com.herofactory.payment.dto.response.PaymentResponse;
import com.herofactory.payment.service.KakaoPayService;
import com.herofactory.subscription.dto.request.SubscriptionRequest;
import com.herofactory.subscription.dto.response.SubscriptionResponse;
import com.herofactory.subscription.entity.*;
import com.herofactory.subscription.entity.enums.SubscriptionStatus;
import com.herofactory.subscription.repository.SubscriptionPlanRepository;
import com.herofactory.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final KakaoPayService kakaoPayService;

    @Transactional(readOnly = true)
    public List<Subscription> findActiveSubscriptionsByPlan(SubscriptionPlan plan) {
        return subscriptionRepository.findByPlanAndStatus(plan, SubscriptionStatus.ACTIVE);
    }

    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        // 기존 구독 확인
        if (subscriptionRepository.hasActiveSubscriptionForPlan(
                request.getMemberId(), request.getPlanId())) {
            throw new SubscriptionException("이미 해당 플랜에 대한 활성 구독이 존재합니다.");
        }

        // 구독 생성
        Subscription subscription = buildSubscription(request);
        subscription = subscriptionRepository.save(subscription);

        // 최초 결제 처리
        processInitialPayment(subscription, request);

        return buildSubscriptionResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse renewSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다."));

        if (!subscription.isRenewable()) {
            throw new SubscriptionException("갱신할 수 없는 구독입니다.");
        }

        // 결제 처리
        try {
            processRenewalPayment(subscription);
            subscription.calculateNextPaymentDate();
            subscription = subscriptionRepository.save(subscription);

            return buildSubscriptionResponse(subscription);
        } catch (Exception e) {
            handlePaymentFailure(subscription);
            throw new SubscriptionException("구독 갱신 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(String subscriptionId, String reason) {
        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다."));

        if (!subscription.isActive()) {
            throw new SubscriptionException("취소할 수 없는 구독 상태입니다.");
        }

        subscription.cancel(reason);
        subscription = subscriptionRepository.save(subscription);

        return buildSubscriptionResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse reactivateSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다."));

        if (!subscription.getStatus().canReactivate()) {
            throw new SubscriptionException("재활성화할 수 없는 구독 상태입니다.");
        }

        // 결제 처리
        try {
            processReactivationPayment(subscription);
            subscription.activate();
            subscription.calculateNextPaymentDate();
            subscription = subscriptionRepository.save(subscription);

            return buildSubscriptionResponse(subscription);
        } catch (Exception e) {
            handlePaymentFailure(subscription);
            throw new SubscriptionException("구독 재활성화 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMemberSubscriptions(String memberId) {
        List<Subscription> subscriptions = subscriptionRepository.findByMemberId(memberId);
        return subscriptions.stream()
                .map(this::buildSubscriptionResponse)
                .toList();
    }

    private Subscription buildSubscription(SubscriptionRequest request) {
        return Subscription.builder()
                .subscriptionId(UUID.randomUUID().toString())
                .memberId(request.getMemberId())
                .plan(getPlanById(request.getPlanId())) // planId 대신 실제 plan 객체를 설정
                .currentPrice(calculateInitialPrice(request)) // amount 대신 price 계산
                .status(SubscriptionStatus.PAYMENT_PENDING)
                .startDate(LocalDateTime.now())
                .nextPaymentDate(LocalDateTime.now())
                .billingCycle(request.getBillingCycle())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Plan 조회를 위한 헬퍼 메서드
    private SubscriptionPlan getPlanById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("구독 플랜을 찾을 수 없습니다: " + planId));
    }

    private Long calculateInitialPrice(SubscriptionRequest request) {
        SubscriptionPlan plan = getPlanById(request.getPlanId());
        return plan.getPriceForPeriod(request.getBillingCycle());
    }

    private void processInitialPayment(Subscription subscription, SubscriptionRequest request) {
        try {
            PaymentRequest paymentRequest = buildPaymentRequest(subscription, request);
            PaymentResponse paymentResponse = kakaoPayService.initiatePayment(paymentRequest);

            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.calculateNextPaymentDate();
            subscriptionRepository.save(subscription);
        } catch (Exception e) {
            handlePaymentFailure(subscription);
            throw new SubscriptionException("초기 결제 처리 중 오류가 발생했습니다.", e);
        }
    }

    private void processRenewalPayment(Subscription subscription) {
        // 정기 결제 처리 로직
        PaymentRequest paymentRequest = buildRenewalPaymentRequest(subscription);
        kakaoPayService.initiatePayment(paymentRequest);
    }

    private void processReactivationPayment(Subscription subscription) {
        // 재활성화 결제 처리 로직
        PaymentRequest paymentRequest = buildReactivationPaymentRequest(subscription);
        kakaoPayService.initiatePayment(paymentRequest);
    }

    private void handlePaymentFailure(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    private PaymentRequest buildPaymentRequest(Subscription subscription, SubscriptionRequest request) {
        return PaymentRequest.builder()
                .memberId(subscription.getMemberId())
                .amount(subscription.getCurrentPrice())
                .itemName(subscription.getPlan().getName())
                .shopItemId(subscription.getPlan().getId())
                .successUrl(request.getSuccessUrl())
                .failUrl(request.getFailUrl())
                .cancelUrl(request.getCancelUrl())
                .build();
    }

    private PaymentRequest buildRenewalPaymentRequest(Subscription subscription) {
        return PaymentRequest.builder()
                .memberId(subscription.getMemberId())
                .amount(subscription.getCurrentPrice())
                .itemName(subscription.getPlan().getName() + " (정기 결제)")
                .shopItemId(subscription.getPlan().getId())
                .build();
    }

    private PaymentRequest buildReactivationPaymentRequest(Subscription subscription) {
        return PaymentRequest.builder()
                .memberId(subscription.getMemberId())
                .amount(subscription.getCurrentPrice())
                .itemName(subscription.getPlan().getName() + " (재활성화)")
                .shopItemId(subscription.getPlan().getId())
                .build();
    }

    private SubscriptionResponse buildSubscriptionResponse(Subscription subscription) {
        // 마지막 결제 정보 조회
        SubscriptionPayment lastPayment = subscription.getPayments().stream()
                .max(Comparator.comparing(SubscriptionPayment::getCreatedAt))
                .orElse(null);

        return SubscriptionResponse.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .memberId(subscription.getMemberId())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getName())
                .amount(subscription.getCurrentPrice())
                .billingCycle(subscription.getBillingCycle())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextPaymentDate(subscription.getNextPaymentDate())
                .cancelledAt(subscription.getCancelledAt())
                .cancelReason(subscription.getCancelReason())
                // 플랜 추가 정보
                .planFeatures(subscription.getPlan().getFeatures())
                .yearlyDiscountPercent(subscription.getPlan().getYearlyDiscountPercent())
                // 결제 정보
                .lastPaymentDate(lastPayment != null ? lastPayment.getCreatedAt() : null)
                .lastPaymentAmount(lastPayment != null ? lastPayment.getAmount() : null)
                // 메타데이터
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Subscription getSubscription(String subscriptionId) {
        return subscriptionRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다: " + subscriptionId));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionStatus(String subscriptionId) {
        Subscription subscription = getSubscription(subscriptionId);
        return buildSubscriptionResponse(subscription);
    }
}
