package ai.herofactoryservice.create_game_resource_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    public static TokenResponse of(String accessToken, Long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}