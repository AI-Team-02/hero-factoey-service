package ai.herofactoryservice.subscription.dto.request;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SmsRequest {
    private String type;
    private String contentType;
    private String countryCode;
    private String from;
    private String content;
    private List<Message> messages;

    @Data
    public static class Message {
        private String to;

        public Message(String to) {
            this.to = to;
        }
    }
}