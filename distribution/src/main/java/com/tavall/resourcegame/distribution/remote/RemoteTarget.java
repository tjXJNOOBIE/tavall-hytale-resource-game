package com.tavall.resourcegame.distribution.remote;

import java.util.Map;
import java.util.Optional;

public record RemoteTarget(
        String targetId,
        String host,
        Optional<String> sshAlias,
        Optional<String> username,
        boolean localTarget,
        Map<String, Integer> ports,
        Map<String, String> metadata
) {
    public RemoteTarget {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId is required.");
        }
        host = host == null || host.isBlank() ? "127.0.0.1" : host;
        sshAlias = sshAlias == null ? Optional.empty() : sshAlias;
        username = username == null ? Optional.empty() : username;
        ports = ports == null ? Map.of() : Map.copyOf(ports);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static RemoteTarget local() {
        return new RemoteTarget("local", "127.0.0.1", Optional.empty(), Optional.empty(), true, Map.of(), Map.of());
    }

    public static RemoteTarget ssh(String targetId, String sshAlias, String host, Map<String, Integer> ports) {
        return new RemoteTarget(targetId, host, Optional.of(sshAlias), Optional.empty(), false, ports, Map.of());
    }

    public String connectHost() {
        if (localTarget) {
            return host;
        }
        return sshAlias.orElse(host);
    }

    public int portOrDefault(String key, int fallback) {
        return ports.getOrDefault(key, fallback);
    }
}
