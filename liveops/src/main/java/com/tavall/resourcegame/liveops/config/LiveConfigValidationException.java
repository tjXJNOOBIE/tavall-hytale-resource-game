package com.tavall.resourcegame.liveops.config;

public final class LiveConfigValidationException extends RuntimeException {
    public LiveConfigValidationException(String message) {
        super(message);
    }

    public LiveConfigValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
