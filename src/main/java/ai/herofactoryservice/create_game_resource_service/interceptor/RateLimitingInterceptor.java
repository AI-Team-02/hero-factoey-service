package ai.herofactoryservice.create_game_resource_service.interceptor;

import ai.herofactoryservice.create_game_resource_service.util.SimpleRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;

@Slf4j
public class RateLimitingInterceptor implements ClientHttpRequestInterceptor {

    private final SimpleRateLimiter rateLimiter;

    public RateLimitingInterceptor(double requestsPerMinute) {  // int를 double로 변경
        this.rateLimiter = new SimpleRateLimiter((int)requestsPerMinute, Duration.ofMinutes(1));
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        try {
            rateLimiter.waitForPermit();

            long startTime = System.currentTimeMillis();
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;

            log.debug("OpenAI API call completed in {}ms", duration);

            return response;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Rate limiting interrupted", e);
        }
    }
}