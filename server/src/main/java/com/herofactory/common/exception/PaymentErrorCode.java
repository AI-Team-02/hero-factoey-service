package com.herofactory.common.exception;

import lombok.Getter;

@Getter
public enum PaymentErrorCode {
    PAYMENT_SYSTEM_ERROR("PAY_001", "결제 시스템 오류가 발생했습니다."),
    INVALID_PAYMENT_AMOUNT("PAY_002", "유효하지 않은 결제 금액입니다."),
    PAYMENT_NOT_FOUND("PAY_003", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_PROCESSED("PAY_004", "이미 처리된 결제입니다."),
    PAYMENT_CANCELLED("PAY_005", "취소된 결제입니다."),
    INVALID_PAYMENT_STATUS("PAY_006", "유효하지 않은 결제 상태입니다."),
    KAKAO_PAY_API_ERROR("PAY_007", "카카오페이 API 호출 중 오류가 발생했습니다."),
    PAYMENT_TIMEOUT("PAY_008", "결제 시간이 초과되었습니다."),
    INSUFFICIENT_BALANCE("PAY_009", "잔액이 부족합니다."),
    INVALID_PAYMENT_METHOD("PAY_010", "유효하지 않은 결제 수단입니다."),
    PAYMENT_LIMIT_EXCEEDED("PAY_011", "결제 한도를 초과했습니다."),
    DUPLICATE_PAYMENT("PAY_012", "중복된 결제입니다.");

    private final String code;
    private final String message;

    PaymentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}