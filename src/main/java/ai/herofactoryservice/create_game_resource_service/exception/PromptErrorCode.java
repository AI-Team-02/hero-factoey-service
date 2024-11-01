package ai.herofactoryservice.create_game_resource_service.exception;

import lombok.Getter;

@Getter
public enum PromptErrorCode {
    PROMPT_SYSTEM_ERROR("PRM_001", "프롬프트 시스템 오류가 발생했습니다."),
    PROMPT_NOT_FOUND("PRM_002", "프롬프트 정보를 찾을 수 없습니다."),
    INVALID_PROMPT_STATUS("PRM_003", "유효하지 않은 프롬프트 상태입니다."),
    PROMPT_PROCESSING_ERROR("PRM_004", "프롬프트 처리 중 오류가 발생했습니다."),
    OPENAI_API_ERROR("PRM_005", "OpenAI API 호출 중 오류가 발생했습니다."),
    INVALID_SKETCH_DATA("PRM_006", "유효하지 않은 스케치 데이터입니다."),
    SKETCH_SIZE_EXCEEDED("PRM_007", "스케치 데이터 크기가 제한을 초과했습니다."),
    PROMPT_TIMEOUT("PRM_008", "프롬프트 처리 시간이 초과되었습니다."),
    DUPLICATE_PROMPT("PRM_009", "중복된 프롬프트입니다."),
    INVALID_PROMPT_REQUEST("PRM_010", "유효하지 않은 프롬프트 요청입니다.");

    private final String code;
    private final String message;

    PromptErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}