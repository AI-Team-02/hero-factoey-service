package com.herofactory.common.exception;


public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
