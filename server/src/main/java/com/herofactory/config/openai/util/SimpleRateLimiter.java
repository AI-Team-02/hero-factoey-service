package com.herofactory.config.openai.util;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleRateLimiter {
    private final int maxRequests;
    private final long timeWindowMillis;
    private final AtomicInteger currentRequests;
    private final AtomicLong windowStartTime;

    public SimpleRateLimiter(int maxRequests, Duration timeWindow) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindow.toMillis();
        this.currentRequests = new AtomicInteger(0);
        this.windowStartTime = new AtomicLong(System.currentTimeMillis());
    }

    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long windowStart = windowStartTime.get();

        if (now - windowStart >= timeWindowMillis) {
            // 새로운 시간 윈도우 시작
            windowStartTime.set(now);
            currentRequests.set(1);
            return true;
        }

        return currentRequests.incrementAndGet() <= maxRequests;
    }

    public void waitForPermit() throws InterruptedException {
        while (!tryAcquire()) {
            Thread.sleep(100); // 100ms 대기 후 재시도
        }
    }
}