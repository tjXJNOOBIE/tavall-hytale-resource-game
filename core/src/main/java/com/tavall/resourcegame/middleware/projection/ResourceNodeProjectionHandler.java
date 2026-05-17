package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.node.ResourceNode;

import java.util.List;

public final class ResourceNodeProjectionHandler implements IProjectionDomain {
    public ResourceNodeProjectionHandler() {
    }

    public ResourceNodeProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectResourceNode(ResourceNode node, GamePlatform platform, List<InteractionAction> actions) {
        return getFrontendProjectionHandler().projectResourceNode(node, platform, actions);
    }
}
