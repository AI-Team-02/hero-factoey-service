package com.herofactory.prompt.entity.enums;

public enum PromptLogType {
    // 프롬프트 생명주기 관련
    CREATED("프롬프트 생성됨"),
    PROCESSING_STARTED("프롬프트 처리 시작"),
    ANALYSIS_STARTED("OpenAI 분석 시작"),
    EMBEDDING_STARTED("임베딩 생성 시작"),
    ANALYSIS_COMPLETED("OpenAI 분석 완료"),
    EMBEDDING_COMPLETED("임베딩 생성 완료"),
    PROCESSING_COMPLETED("프롬프트 처리 완료"),
    PROCESSING_FAILED("프롬프트 처리 실패"),

    // 메시지 처리 관련
    MESSAGE_SENT("메시지 발송됨"),
    MESSAGE_RECEIVED("메시지 수신됨"),
    MESSAGE_PROCESSING("메시지 처리 중"),
    MESSAGE_PROCESSED("메시지 처리 완료"),
    MESSAGE_FAILED("메시지 처리 실패"),
    MESSAGE_REQUEUED("메시지 재처리 대기");

    private final String description;

    PromptLogType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}