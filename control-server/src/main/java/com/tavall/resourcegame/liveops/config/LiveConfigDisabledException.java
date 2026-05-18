package org.tavall.control.liveops.config;

public final class LiveConfigDisabledException extends RuntimeException {
    public LiveConfigDisabledException(String message) {
        super(message);
    }
}
