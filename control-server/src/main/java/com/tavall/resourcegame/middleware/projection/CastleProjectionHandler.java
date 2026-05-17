package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.castle.Castle;
import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.List;

public final class CastleProjectionHandler implements IProjectionDomain {
    public CastleProjectionHandler() {
    }

    public CastleProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectCastle(Castle castle, GamePlatform platform, List<InteractionAction> actions) {
        return getFrontendProjectionHandler().projectCastle(castle, platform, actions);
    }
}
