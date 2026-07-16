package org.tavall.control.distribution.remote;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RemoteCommand(
        String commandId,
        String executable,
        List<String> arguments,
        Duration timeout,
        boolean highRisk,
        Map<String, String> metadata
) {
    public RemoteCommand {
        commandId = commandId == null || commandId.isBlank() ? UUID.randomUUID().toString() : commandId;
        if (executable == null || executable.isBlank()) {
            throw new IllegalArgumentException("executable is required.");
        }
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
        timeout = timeout == null ? Duration.ofSeconds(15) : timeout;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static RemoteCommand safe(String executable, String... arguments) {
        return new RemoteCommand(UUID.randomUUID().toString(), executable, List.of(arguments), Duration.ofSeconds(15), false, Map.of());
    }
}
