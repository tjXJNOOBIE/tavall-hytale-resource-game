package org.tavall.control.projection;

import org.tavall.control.castle.Castle;
import org.tavall.control.common.GamePlatform;

import java.util.List;

public final class CastleProjectionHandler implements ProjectionDomain {
    public CastleProjectionHandler() {
    }

    public CastleProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectCastle(Castle castle, GamePlatform platform, List<InteractionAction> actions) {
        return getFrontendProjectionHandler().projectCastle(castle, platform, actions);
    }
}
