package org.tavall.control.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ReverseProxyRouteCreationHandler implements IReverseProxyRouteCreationHandler, CloudControlDomain {
    @Override
    public ReverseProxyRoute createRoute(UUID workloadId, UUID nodeId, String hostname, Optional<String> pathPrefix, String targetHost, int targetPort, boolean tlsEnabled) {
        if (hostname == null || hostname.isBlank()) {
            throw new IllegalArgumentException("Reverse proxy hostname is required.");
        }
        if (targetPort <= 0 || targetPort > 65535) {
            throw new IllegalArgumentException("Reverse proxy target port must be between 1 and 65535.");
        }
        ReverseProxyRoute route = new ReverseProxyRoute(UUID.randomUUID(), workloadId, nodeId, hostname, pathPrefix,
                targetHost, targetPort, tlsEnabled, CloudDesiredState.DESIRED, CloudDesiredState.DESIRED, Map.of());
        getCloudRepository().saveReverseProxyRoute(route);
        return route;
    }
}
