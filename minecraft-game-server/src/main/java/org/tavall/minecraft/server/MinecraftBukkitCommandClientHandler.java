package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.util.Map;

public final class MinecraftBukkitCommandClientHandler implements IMinecraftBukkitCommandClientHandler, MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
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
