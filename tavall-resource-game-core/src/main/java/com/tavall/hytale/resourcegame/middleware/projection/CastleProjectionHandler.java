package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.castle.Castle;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.util.List;

public final class CastleProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public CastleProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectCastle(Castle castle, GamePlatform platform, List<InteractionAction> actions) {
        return frontendProjectionHandler.projectCastle(castle, platform, actions);
    }
}
