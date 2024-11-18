package com.herofactory.subscription.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanRequest {

    @NotBlank(message = "플랜 이름은 필수입니다")
    @Size(max = 100, message = "플랜 이름은 100자를 초과할 수 없습니다")
    private String name;

    @NotBlank(message = "플랜 설명은 필수입니다")
    @Size(max = 500, message = "플랜 설명은 500자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "월간 가격은 필수입니다")
    @Min(value = 0, message = "월간 가격은 0보다 커야 합니다")
    private Long monthlyPrice;

    @NotNull(message = "연간 할인율은 필수입니다")
    @Min(value = 0, message = "연간 할인율은 0보다 커야 합니다")
    private Integer yearlyDiscountPercent;

    @NotNull(message = "플랜 기능 목록은 필수입니다")
    private List<String> features;

    // 선택적 필드
    private Boolean isActive;
}