package org.tavall.control.liveops.config;

public final class LiveConfigValidationException extends RuntimeException {
    public LiveConfigValidationException(String message) {
        super(message);
    }

    public LiveConfigValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
