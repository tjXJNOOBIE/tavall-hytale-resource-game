package org.tavall.control.events.middleware;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

public record EventRateLimitPolicy(int maxEvents, Duration window, Clock clock) {
    public EventRateLimitPolicy {
        if (maxEvents <= 0) {
            throw new IllegalArgumentException("maxEvents must be positive.");
        }
        window = Objects.requireNonNull(window, "window");
        clock = Objects.requireNonNull(clock, "clock");
    }
}
