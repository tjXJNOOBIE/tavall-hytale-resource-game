package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ReverseProxyRoute(
        UUID routeId,
        UUID workloadId,
        UUID nodeId,
        String hostname,
        Optional<String> pathPrefix,
        String targetHost,
        int targetPort,
        boolean tlsEnabled,
        CloudDesiredState desiredState,
        CloudDesiredState actualState,
        Map<String, String> metadata
) {
    public ReverseProxyRoute {
        pathPrefix = pathPrefix == null ? Optional.empty() : pathPrefix;
        targetHost = targetHost == null || targetHost.isBlank() ? "127.0.0.1" : targetHost;
        desiredState = desiredState == null ? CloudDesiredState.DESIRED : desiredState;
        actualState = actualState == null ? CloudDesiredState.DESIRED : actualState;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
