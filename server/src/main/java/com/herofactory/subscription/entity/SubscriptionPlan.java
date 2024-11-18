package com.herofactory.subscription.entity;

import com.herofactory.subscription.entity.enums.BillingCycle;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // 플랜 이름 (예: "베이직", "프로", "엔터프라이즈")

    @Column(nullable = false)
    private String description; // 플랜 설명

    @Column(nullable = false)
    private Long monthlyPrice;  // 월간 구독 가격

    @Column(nullable = false)
    private Long yearlyPrice;   // 연간 구독 가격 (보통 월간 x 12 * 할인율)

    @Column(nullable = false)
    private Integer yearlyDiscountPercent; // 연간 구독 할인율

    @ElementCollection
    @CollectionTable(name = "plan_features",
            joinColumns = @JoinColumn(name = "plan_id"))
    private List<String> features = new ArrayList<>(); // 플랜별 제공 기능

    @Column(nullable = false)
    private Boolean isActive;   // 플랜 활성화 여부

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getPriceForPeriod(BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> monthlyPrice;
            case YEARLY -> yearlyPrice;
        };
    }
}
