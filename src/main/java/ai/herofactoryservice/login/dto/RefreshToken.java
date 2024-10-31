package ai.herofactoryservice.login.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshToken {
    private String refreshToken;
}