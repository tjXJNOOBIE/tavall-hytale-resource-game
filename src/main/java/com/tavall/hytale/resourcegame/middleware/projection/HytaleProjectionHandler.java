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

public final class HytaleProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public HytaleProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectCastleForHytaleClient(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.HYTALE, List.of(action("hytale.castle.open", "Open", PlatformInteractionType.HYTALE_UI_ACTION)));
    }

    public FrontendProjection projectResourceNodeForHytaleClient(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.HYTALE, List.of(action("hytale.node.interact", "Interact", PlatformInteractionType.HYTALE_ENTITY_INTERACT)));
    }

    public FrontendProjection projectGuildForHytaleClient(GuildKingdom guildKingdom) {
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.HYTALE, List.of(action("hytale.guild.summary", "Summary", PlatformInteractionType.HYTALE_UI_ACTION)));
    }

    public FrontendProjection projectPetitionForHytaleClient(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.HYTALE, List.of(action("hytale.petition.bot_verify", "Bot Verify", PlatformInteractionType.HYTALE_BOT_TEST_ACTION)));
    }

    private InteractionAction action(String id, String label, PlatformInteractionType type) {
        return new InteractionAction(id, label, Optional.empty(), Set.of(), true, Optional.empty(), type, Map.of());
    }
}
