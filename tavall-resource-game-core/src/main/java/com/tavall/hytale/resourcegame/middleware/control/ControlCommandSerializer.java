package com.tavall.hytale.resourcegame.middleware.control;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ControlCommandSerializer {
    private static final Set<String> SENSITIVE_KEYS = Set.of("token", "secret", "password", "auth", "session", "identity");

    public Map<String, String> redactedArguments(ControlCommand command) {
        LinkedHashMap<String, String> redacted = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : command.arguments().entrySet()) {
            redacted.put(entry.getKey(), isSensitive(entry.getKey()) ? "[REDACTED]" : entry.getValue());
        }
        return Map.copyOf(redacted);
    }

    private boolean isSensitive(String key) {
        String lowered = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lowered::contains);
    }
}
