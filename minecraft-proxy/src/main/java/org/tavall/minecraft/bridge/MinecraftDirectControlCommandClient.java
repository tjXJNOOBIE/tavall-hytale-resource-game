package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.IDependencyInjectableConcrete;

public final class MinecraftDirectControlCommandClient implements IMinecraftControlCommandClient, IFrontendControlCommandClient, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitCommand(envelope);
    }

    @Override
    public RankResponse submitRankRequest(RankRequest request) {
        return DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitRankRequest(request);
    }

    @Override
    public PunishResponse submitPunishRequest(PunishRequest request) {
        return DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class).submitPunishRequest(request);
    }
}
