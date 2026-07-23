package org.tavall.minecraft.server;

import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.dependency.IDependencyInjectableInterface;

import java.io.IOException;
import java.util.Map;

public interface IMinecraftBukkitCommandClientHandler extends IDependencyInjectableInterface {
    FrontendCommandVerificationResult submitCommand(
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    ) throws IOException;

    InteractionResult submitInteraction(InteractionRequest request) throws IOException;

    PlayerDataResponse fetchPlayerData(PlayerDataRequest request) throws IOException;
}
