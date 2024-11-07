package ai.herofactoryservice.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoPayReadyResponse {
    private String tid;                   // 결제 고유 번호

    @JsonProperty("next_redirect_pc_url")
    private String nextRedirectPcUrl;     // PC 웹 결제 페이지 URL

    @JsonProperty("next_redirect_mobile_url")
    private String nextRedirectMobileUrl; // 모바일 웹 결제 페이지 URL

    @JsonProperty("next_redirect_app_url")
    private String nextRedirectAppUrl;    // 모바일 앱 결제 페이지 URL

    @JsonProperty("created_at")
    private String createdAt;             // 결제 준비 요청 시간
}