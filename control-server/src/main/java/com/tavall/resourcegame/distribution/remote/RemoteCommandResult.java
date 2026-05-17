package com.tavall.resourcegame.distribution.remote;

import java.time.Instant;
import java.util.Map;

public record RemoteCommandResult(
        String commandId,
        String targetId,
        boolean successful,
        boolean timedOut,
        int exitCode,
        String stdout,
        String stderr,
        Instant startedAt,
        Instant completedAt,
        Map<String, String> metadata
) {
    public RemoteCommandResult {
        stdout = stdout == null ? "" : stdout;
        stderr = stderr == null ? "" : stderr;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
