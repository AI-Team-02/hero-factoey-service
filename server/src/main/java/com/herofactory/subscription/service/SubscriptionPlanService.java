package com.herofactory.subscription.service;

import com.herofactory.common.exception.PlanNotFoundException;
import com.herofactory.subscription.dto.request.SubscriptionPlanRequest;
import com.herofactory.subscription.entity.*;
import com.herofactory.subscription.entity.enums.BillingCycle;
import com.herofactory.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public SubscriptionPlan createPlan(SubscriptionPlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .monthlyPrice(request.getMonthlyPrice())
                .yearlyPrice(calculateYearlyPrice(
                        request.getMonthlyPrice(),
                        request.getYearlyDiscountPercent()
                ))
                .yearlyDiscountPercent(request.getYearlyDiscountPercent())
                .features(request.getFeatures())
                .isActive(true)
                .build();

        return planRepository.save(plan);
    }

    @Transactional
    public SubscriptionPlan updatePlan(Long planId, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("플랜을 찾을 수 없습니다."));

        // 가격이 변경된 경우
        if (!plan.getMonthlyPrice().equals(request.getMonthlyPrice()) ||
                !plan.getYearlyDiscountPercent().equals(request.getYearlyDiscountPercent())) {

            handlePriceChange(plan, request);
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setFeatures(request.getFeatures());

        return planRepository.save(plan);
    }

    private void handlePriceChange(SubscriptionPlan plan, SubscriptionPlanRequest request) {
        // 기존 구독자들에 대한 처리
        List<Subscription> activeSubscriptions =
                subscriptionService.findActiveSubscriptionsByPlan(plan);

        for (Subscription subscription : activeSubscriptions) {
            if (subscription.getBillingCycle() == BillingCycle.MONTHLY) {
                // 월간 구독자
                subscription.setCurrentPrice(request.getMonthlyPrice());
            } else {
                // 연간 구독자
                subscription.setCurrentPrice(calculateYearlyPrice(
                        request.getMonthlyPrice(),
                        request.getYearlyDiscountPercent()
                ));
            }
        }

        // 새 가격 설정
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(calculateYearlyPrice(
                request.getMonthlyPrice(),
                request.getYearlyDiscountPercent()
        ));
        plan.setYearlyDiscountPercent(request.getYearlyDiscountPercent());
    }

    private Long calculateYearlyPrice(Long monthlyPrice, Integer discountPercent) {
        long yearlyBeforeDiscount = monthlyPrice * 12;
        return yearlyBeforeDiscount * (100 - discountPercent) / 100;
    }
}