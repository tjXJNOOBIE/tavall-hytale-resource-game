package org.tavall.control.projection;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.node.ResourceNode;

import java.util.List;

public final class ResourceNodeProjectionHandler implements ProjectionDomain {
    public ResourceNodeProjectionHandler() {
    }

    public ResourceNodeProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectResourceNode(ResourceNode node, GamePlatform platform, List<InteractionAction> actions) {
        return getFrontendProjectionHandler().projectResourceNode(node, platform, actions);
    }
}
