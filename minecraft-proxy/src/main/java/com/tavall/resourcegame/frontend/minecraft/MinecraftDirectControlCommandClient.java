package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.dependency.interfaces.IFrontendControlCommandClient;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.PunishResponse;
import com.tavall.resourcegame.shared.frontend.RankRequest;
import com.tavall.resourcegame.shared.frontend.RankResponse;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class MinecraftDirectControlCommandClient implements IMinecraftControlCommandClient, IFrontendControlCommandClient, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return com.tavall.resourcegame.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitCommand(envelope);
    }

    @Override
    public RankResponse submitRankRequest(RankRequest request) {
        return com.tavall.resourcegame.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitRankRequest(request);
    }

    @Override
    public PunishResponse submitPunishRequest(PunishRequest request) {
        return com.tavall.resourcegame.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitPunishRequest(request);
    }
}
