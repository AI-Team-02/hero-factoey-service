//package ai.herofactoryservice.subscription.entity;
//
//import ai.herofactoryservice.subscription.entity.enums.BillingCycle;
//import ai.herofactoryservice.subscription.entity.enums.SubscriptionStatus;
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "subscriptions")
//@Getter @Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Subscription {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, length = 36)
//    private String subscriptionId;
//
//    @Column(nullable = false)
//    private String memberId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "plan_id", nullable = false)
//    private SubscriptionPlan plan;
//
//    @Column(nullable = false)
//    private Long currentPrice;  // 현재 지불하고 있는 가격
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private BillingCycle billingCycle;  // MONTHLY/YEARLY
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private SubscriptionStatus status;
//
//    @Column(nullable = false)
//    private LocalDateTime startDate;
//
//    private LocalDateTime endDate;
//
//    @Column(nullable = false)
//    private LocalDateTime nextPaymentDate;
//
//    private String cancelReason;
//    private LocalDateTime cancelledAt;
//
//    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
//    private List<SubscriptionPayment> payments = new ArrayList<>();
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    public void calculateNextPaymentDate() {
//        this.nextPaymentDate = this.billingCycle.calculateNextPaymentDate(
//                this.nextPaymentDate != null ? this.nextPaymentDate : LocalDateTime.now()
//        );
//    }
//
//    public boolean isEligibleForUpgrade(SubscriptionPlan newPlan) {
//        return this.plan.getMonthlyPrice() < newPlan.getMonthlyPrice();
//    }
//
//    public boolean isEligibleForDowngrade(SubscriptionPlan newPlan) {
//        return this.plan.getMonthlyPrice() > newPlan.getMonthlyPrice();
//    }
//
//    public Long calculateProrationAmount(SubscriptionPlan newPlan) {
//        if (!this.isActive()) {
//            return 0L;
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        long daysUntilNextPayment = ChronoUnit.DAYS.between(now, this.nextPaymentDate);
//        long totalDays = ChronoUnit.DAYS.between(
//                this.nextPaymentDate.minus(1, this.billingCycle.getChronoUnit()),
//                this.nextPaymentDate
//        );
//
//        long currentPlanRemainingAmount =
//                (this.currentPrice * daysUntilNextPayment) / totalDays;
//
//        long newPlanRemainingAmount =
//                (newPlan.getPriceForPeriod(this.billingCycle) * daysUntilNextPayment) / totalDays;
//
//        return newPlanRemainingAmount - currentPlanRemainingAmount;
//    }
//
//    // 플랜 변경 메서드
//    public void changePlan(SubscriptionPlan newPlan, boolean immediate) {
//        if (immediate) {
//            this.plan = newPlan;
//            this.currentPrice = newPlan.getPriceForPeriod(this.billingCycle);
//            calculateNextPaymentDate();
//        } else {
//            // 다음 결제일에 적용될 플랜 변경 예약
//            this.setPendingPlanChange(newPlan);
//        }
//    }
//
//    // 구독 갱신
//    public void renew() {
//        if (this.getPendingPlanChange() != null) {
//            this.plan = this.getPendingPlanChange();
//            this.currentPrice = this.plan.getPriceForPeriod(this.billingCycle);
//            this.setPendingPlanChange(null);
//        }
//        calculateNextPaymentDate();
//    }
//
//    // 결제 주기 변경
//    public void changeBillingCycle(BillingCycle newCycle) {
//        this.billingCycle = newCycle;
//        this.currentPrice = this.plan.getPriceForPeriod(newCycle);
//        calculateNextPaymentDate();
//    }
//}