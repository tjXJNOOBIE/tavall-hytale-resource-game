package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlCommandClient;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.InteractionRequest;
import com.tavall.resourcegame.shared.frontend.InteractionResult;
import com.tavall.resourcegame.shared.frontend.PlayerDataRequest;
import com.tavall.resourcegame.shared.frontend.PlayerDataResponse;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.util.Map;

public final class MinecraftBukkitCommandClientHandler implements IMinecraftBukkitCommandClientHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitCommand(
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    ) throws IOException {
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                platformAccountId,
                platformDisplayName,
                rawInput,
                correlationId,
                sourceMetadata
        );
        return controlCommandClient().submitCommand(envelope);
    }

    @Override
    public InteractionResult submitInteraction(InteractionRequest request) throws IOException {
        return controlCommandClient().submitInteraction(request);
    }

    @Override
    public PlayerDataResponse fetchPlayerData(PlayerDataRequest request) throws IOException {
        return controlCommandClient().fetchPlayerData(request);
    }

    private IFrontendControlCommandClient controlCommandClient() {
        return DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class);
    }
}
