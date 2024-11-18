package com.herofactory.subscription.repository;

import com.herofactory.subscription.entity.Subscription;
import com.herofactory.subscription.entity.SubscriptionPlan;
import com.herofactory.subscription.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriptionId(String subscriptionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.subscriptionId = :subscriptionId")
    Optional<Subscription> findBySubscriptionIdWithLock(@Param("subscriptionId") String subscriptionId);

    List<Subscription> findByMemberId(String memberId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    boolean existsByMemberIdAndStatus(String memberId, SubscriptionStatus status);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.memberId = :memberId " +
            "AND s.status = 'ACTIVE' AND s.plan.id = :planId")
    boolean hasActiveSubscriptionForPlan(@Param("memberId") String memberId,
                                         @Param("planId") Long planId);

    List<Subscription> findByPlanAndStatus(SubscriptionPlan plan, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.nextPaymentDate <= :date " +
            "AND s.status = 'ACTIVE'")
    List<Subscription> findDueSubscriptions(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Subscription s WHERE s.nextPaymentDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'ACTIVE'")
    List<Subscription> findUpcomingRenewals(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}