//package ai.herofactoryservice.subscription.service;
//
//import ai.herofactoryservice.common.exception.SubscriptionException;
//import ai.herofactoryservice.payment.service.KakaoPayService;
//import ai.herofactoryservice.subscription.entity.*;
//import ai.herofactoryservice.subscription.repository.SubscriptionRepository;
//import ai.herofactoryservice.subscription.dto.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SubscriptionService {
//    private final SubscriptionRepository subscriptionRepository;
//    private final KakaoPayService kakaoPayService;
//
//    @Transactional
//    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
//        // 기존 구독 확인
//        if (subscriptionRepository.hasActiveSubscriptionForPlan(
//                request.getMemberId(), request.getPlanId())) {
//            throw new SubscriptionException("이미 해당 플랜에 대한 활성 구독이 존재합니다.");
//        }
//
//        // 구독 생성
//        Subscription subscription = buildSubscription(request);
//        subscription = subscriptionRepository.save(subscription);
//
//        // 최초 결제 처리
//        processInitialPayment(subscription, request);
//
//        return buildSubscriptionResponse(subscription);
//    }
//
//    @Transactional
//    public SubscriptionResponse renewSubscription(String subscriptionId) {
//        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
//                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다."));
//
//        if (!subscription.isRenewable()) {
//            throw new SubscriptionException("갱신할 수 없는 구독입니다.");
//        }
//
//        // 결제 처리
//        try {
//            processRenewalPayment(subscription);
//            subscription.calculateNextPaymentDate();
//            subscription = subscriptionRepository.save(subscription);
//
//            return buildSubscriptionResponse(subscription);
//        } catch (Exception e) {
//            handlePaymentFailure(subscription);
//            throw new SubscriptionException("구독 갱신 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    @Transactional
//    public SubscriptionResponse cancelSubscription(String subscriptionId, String reason) {
//        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
//                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다."));
//
//        if (!subscription.isActive()) {
//            throw new SubscriptionException("취소할 수 없는 구독 상태입니다.");
//        }
//
//        subscription.cancel(reason);
//        subscription = subscriptionRepository.save(subscription);
//
//        return buildSubscriptionResponse(subscription);
//    }
//
//    @Transactional
//    public SubscriptionResponse reactivateSubscription(String subscriptionId) {
//        Subscription subscription = subscriptionRepository.findBySubscriptionIdWithLock(subscriptionId)
//                .orElseThrow(() -> new SubscriptionException("구독 정보를 찾을 수 없습니다."));
//
//        if (!subscription.getStatus().canReactivate()) {
//            throw new SubscriptionException("재활성화할 수 없는 구독 상태입니다.");
//        }
//
//        // 결제 처리
//        try {
//            processReactivationPayment(subscription);
//            subscription.activate();
//            subscription.calculateNextPaymentDate();
//            subscription = subscriptionRepository.save(subscription);
//
//            return buildSubscriptionResponse(subscription);
//        } catch (Exception e) {
//            handlePaymentFailure(subscription);
//            throw new SubscriptionException("구독 재활성화 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public List<SubscriptionResponse> getMemberSubscriptions(String memberId) {
//        List<Subscription> subscriptions = subscriptionRepository.findByMemberId(memberId);
//        return subscriptions.stream()
//                .map(this::buildSubscriptionResponse)
//                .toList();
//    }
//
//    // Private helper methods
//    private Subscription buildSubscription(SubscriptionRequest request) {
//        return Subscription.builder()
//                .subscriptionId(UUID.randomUUID().toString())
//                .memberId(request.getMemberId())
//                .planId(request.getPlanId())
//                .planName(request.getPlanName())
//                .amount(request.getAmount())
//                .status(SubscriptionStatus.PAYMENT_PENDING)
//                .startDate(LocalDateTime.now())
//                .nextPaymentDate(LocalDateTime.now())
//                .billingCycle(request.getBillingCycle())
//                .billingPeriod(request.getBillingPeriod())
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//
//    private void processInitialPayment(Subscription subscription, SubscriptionRequest request) {
//        try {
//            PaymentRequest paymentRequest = buildPaymentRequest(subscription, request);
//            PaymentResponse paymentResponse = kakaoPayService.initiatePayment(paymentRequest);
//
//            subscription.setStatus(SubscriptionStatus.ACTIVE);
//            subscription.calculateNextPaymentDate();
//            subscriptionRepository.save(subscription);
//        } catch (Exception e) {
//            handlePaymentFailure(subscription);
//            throw new SubscriptionException("초기 결제 처리 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    private void processRenewalPayment(Subscription subscription) {
//        // 정기 결제 처리 로직
//        PaymentRequest paymentRequest = buildRenewalPaymentRequest(subscription);
//        kakaoPayService.initiatePayment(paymentRequest);
//    }
//
//    private void processReactivationPayment(Subscription subscription) {
//        // 재활성화 결제 처리 로직
//        PaymentRequest paymentRequest = buildReactivationPaymentRequest(subscription);
//        kakaoPayService.initiatePayment(paymentRequest);
//    }
//
//    private void handlePaymentFailure(Subscription subscription) {
//        subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
//        subscription.setUpdatedAt(LocalDateTime.now());
//        subscriptionRepository.save(subscription);
//    }
//
//    private PaymentRequest buildPaymentRequest(Subscription subscription, SubscriptionRequest request) {
//        return PaymentRequest.builder()
//                .memberId(subscription.getMemberId())
//                .amount(subscription.getAmount())
//                .itemName(subscription.getPlanName())
//                .shopItemId(subscription.getPlanId())
//                .successUrl(request.getSuccessUrl())
//                .failUrl(request.getFailUrl())
//                .cancelUrl(request.getCancelUrl())
//                .build();
//    }
//
//    private PaymentRequest buildRenewalPaymentRequest(Subscription subscription) {
//        return PaymentRequest.builder()
//                .memberId(subscription.getMemberId())
//                .amount(subscription.getAmount())
//                .itemName(subscription.getPlanName() + " (정기 결제)")
//                .shopItemId(subscription.getPlanId())
//                .build();
//    }
//
//    private PaymentRequest buildReactivationPaymentRequest(Subscription subscription) {
//        return PaymentRequest.builder()
//                .memberId(subscription.getMemberId())
//                .amount(subscription.getAmount())
//                .itemName(subscription.getPlanName() + " (재활성화)")
//                .shopItemId(subscription.getPlanId())
//                .build();
//    }
//
//    private SubscriptionResponse buildSubscriptionResponse(Subscription subscription) {
//        return SubscriptionResponse.builder()
//                .subscriptionId(subscription.getSubscriptionId())
//                .memberId(subscription.getMemberId())
//                .planId(subscription.getPlanId())
//                .planName(subscription.getPlanName())
//                .amount(subscription.getAmount())
//                .status(subscription.getStatus())
//                .startDate(subscription.getStartDate())
//                .endDate(subscription.getEndDate())
//                .nextPaymentDate(subscription.getNextPaymentDate())
//                .billingCycle(subscription.getBillingCycle())
//                .build();
//    }
//}
