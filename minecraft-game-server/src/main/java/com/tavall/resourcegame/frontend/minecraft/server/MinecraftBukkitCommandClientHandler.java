package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionRequest;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.player.PlayerDataRequest;
import com.tavall.resourcegame.api.internal.player.PlayerDataResponse;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;
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
