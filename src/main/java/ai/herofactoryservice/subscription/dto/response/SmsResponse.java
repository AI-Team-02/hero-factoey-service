package ai.herofactoryservice.subscription.dto.response;

import lombok.Data;

@Data
public class SmsResponse {
    private String requestId;
    private String requestTime;
    private String statusCode;
    private String statusName;
    private String messageId;
}
