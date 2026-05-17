package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionRequest;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.player.PlayerDataRequest;
import com.tavall.resourcegame.api.internal.player.PlayerDataResponse;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

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
