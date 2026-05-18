package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
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
