package ai.herofactoryservice.subscription.entity.enums;

import java.time.LocalDateTime;

public enum BillingCycle {
    MONTHLY("월간", 1),
    YEARLY("연간", 12);

    private final String description;
    private final int monthCount;

    BillingCycle(String description, int monthCount) {
        this.description = description;
        this.monthCount = monthCount;
    }

    public String getDescription() {
        return description;
    }

    public int getMonthCount() {
        return monthCount;
    }

    public LocalDateTime calculateNextPaymentDate(LocalDateTime from) {
        return switch (this) {
            case MONTHLY -> from.plusMonths(1);
            case YEARLY -> from.plusYears(1);
        };
    }
}