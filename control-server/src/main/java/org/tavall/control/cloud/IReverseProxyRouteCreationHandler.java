package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.Optional;
import java.util.UUID;

public interface IReverseProxyRouteCreationHandler extends IDependencyInjectableInterface {
    ReverseProxyRoute createRoute(UUID workloadId, UUID nodeId, String hostname, Optional<String> pathPrefix, String targetHost, int targetPort, boolean tlsEnabled);
}
