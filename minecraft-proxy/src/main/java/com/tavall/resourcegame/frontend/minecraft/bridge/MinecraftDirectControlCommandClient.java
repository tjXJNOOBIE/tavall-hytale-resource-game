package com.tavall.resourcegame.frontend.minecraft.bridge;

import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.permissions.PunishRequest;
import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.api.internal.permissions.RankRequest;
import com.tavall.resourcegame.api.internal.permissions.RankResponse;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class MinecraftDirectControlCommandClient implements IMinecraftControlCommandClient, IFrontendControlCommandClient, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitCommand(envelope);
    }

    @Override
    public RankResponse submitRankRequest(RankRequest request) {
        return com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitRankRequest(request);
    }

    @Override
    public PunishResponse submitPunishRequest(PunishRequest request) {
        return com.tjxjnoobie.api.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitPunishRequest(request);
    }
}
