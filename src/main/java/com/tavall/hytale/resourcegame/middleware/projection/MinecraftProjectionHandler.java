package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.castle.Castle;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.node.ResourceNode;
import com.tavall.hytale.resourcegame.middleware.petition.Petition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MinecraftProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public MinecraftProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectCastleForMinecraftClient(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.MINECRAFT, List.of(action("kingdom.castle.info", "Castle", PlatformInteractionType.MINECRAFT_COMMAND)));
    }

    public FrontendProjection projectResourceNodeForMinecraftClient(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.MINECRAFT, List.of(action("kingdom.node.collect", "Collect", PlatformInteractionType.MINECRAFT_ENTITY_INTERACT)));
    }

    public FrontendProjection projectGuildForMinecraftClient(GuildKingdom guildKingdom) {
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.MINECRAFT, List.of(action("kingdom.info", "Info", PlatformInteractionType.MINECRAFT_COMMAND)));
    }

    public FrontendProjection projectPetitionForMinecraftClient(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.MINECRAFT, List.of(action("kingdom.petition.fund", "Fund", PlatformInteractionType.MINECRAFT_COMMAND)));
    }

    private InteractionAction action(String id, String label, PlatformInteractionType type) {
        return new InteractionAction(id, label, Optional.empty(), Set.of(), true, Optional.empty(), type, Map.of());
    }
}
