package ai.herofactoryservice.create_game_resource_service.exception;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}