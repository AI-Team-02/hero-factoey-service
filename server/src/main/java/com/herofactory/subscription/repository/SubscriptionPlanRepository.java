package com.herofactory.subscription.repository;

import com.herofactory.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    // 기본적인 findById는 JpaRepository에서 이미 제공하므로 삭제 가능
    // Optional<SubscriptionPlan> findById(Long id); - 삭제

    // 활성화된 플랜만 조회하는 메서드들
    List<SubscriptionPlan> findByIsActiveTrue();
    Optional<SubscriptionPlan> findByIdAndIsActiveTrue(Long id);

    // 특정 가격 이하의 활성화된 플랜 조회
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.monthlyPrice <= :maxPrice AND p.isActive = true")
    List<SubscriptionPlan> findAvailablePlansUnderPrice(@Param("maxPrice") Long maxPrice);

    // 업그레이드 가능한 플랜 조회
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.monthlyPrice > " +
            "(SELECT currentPlan.monthlyPrice FROM SubscriptionPlan currentPlan WHERE currentPlan.id = :currentPlanId) " +
            "AND p.isActive = true ORDER BY p.monthlyPrice ASC")
    List<SubscriptionPlan> findAvailableUpgradePlans(@Param("currentPlanId") Long currentPlanId);
}