package com.herofactory.prompt.entity.enums;

public enum PromptStatus {
    PENDING,        // 초기 상태
    PROCESSING,     // 처리 중
    COMPLETED,      // 완료됨
    FAILED;         // 실패

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean canProcess() {
        return this == PENDING;
    }
}