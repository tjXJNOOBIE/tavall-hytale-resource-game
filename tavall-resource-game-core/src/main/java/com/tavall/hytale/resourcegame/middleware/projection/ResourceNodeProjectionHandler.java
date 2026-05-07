package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.node.ResourceNode;

import java.util.List;

public final class ResourceNodeProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public ResourceNodeProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectResourceNode(ResourceNode node, GamePlatform platform, List<InteractionAction> actions) {
        return frontendProjectionHandler.projectResourceNode(node, platform, actions);
    }
}
