package com.herofactory.common.logging.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiAccessLog {
    private String loggedAt;
    private String endpoint;         // API 경로
    private String method;          // HTTP 메소드
    private String clientIp;        // 클라이언트 IP
    private String userAgent;       // User-Agent
    private String userId;          // 인증된 사용자 ID (없으면 null)
    private Long responseTime;      // 응답 시간 (ms)
    private Integer statusCode;     // HTTP 상태 코드
    private String errorMessage;    // 에러 발생시 메시지
    private Map<String, Object> additionalInfo;  // 추가 정보를 위한 유연한 필드
}