package ai.herofactoryservice.create_game_resource_service.monitoring;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PromptMetrics {
    private final MeterRegistry registry;

    private final Counter promptRequestCounter;
    private final Counter promptSuccessCounter;
    private final Counter promptFailureCounter;
    private final Timer promptProcessingTimer;
    private final Gauge promptQueueSize;

    public PromptMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.promptRequestCounter = Counter.builder("prompt.requests.total")
                .description("Total number of prompt requests")
                .register(registry);

        this.promptSuccessCounter = Counter.builder("prompt.requests.success")
                .description("Number of successful prompt requests")
                .register(registry);

        this.promptFailureCounter = Counter.builder("prompt.requests.failure")
                .description("Number of failed prompt requests")
                .register(registry);

        this.promptProcessingTimer = Timer.builder("prompt.processing.time")
                .description("Time taken to process prompts")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.promptQueueSize = Gauge.builder("prompt.queue.size", this, PromptMetrics::getQueueSize)
                .description("Current size of the prompt processing queue")
                .register(registry);
    }

    public void recordRequest() {
        promptRequestCounter.increment();
    }

    public void recordSuccess() {
        promptSuccessCounter.increment();
    }

    public void recordFailure() {
        promptFailureCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(promptProcessingTimer);
    }

    public void recordProcessingTime(long timeMs) {
        promptProcessingTimer.record(timeMs, TimeUnit.MILLISECONDS);
    }
    // Implement this method based on your queue implementation
    private int getQueueSize() {
        // Return current queue size from your message queue
        return 0;
    }

    public void recordTokenUsage(int tokens) {
        registry.counter("prompt.tokens.used").increment(tokens);
    }
}