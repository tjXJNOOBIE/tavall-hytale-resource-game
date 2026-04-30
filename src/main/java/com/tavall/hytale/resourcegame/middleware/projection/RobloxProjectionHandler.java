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

public final class RobloxProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public RobloxProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectCastleForRobloxClient(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.ROBLOX, List.of(action("roblox.castle.prompt", "Castle", PlatformInteractionType.ROBLOX_PROXIMITY_PROMPT)));
    }

    public FrontendProjection projectResourceNodeForRobloxClient(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.ROBLOX, List.of(action("roblox.node.remote_event", "Node", PlatformInteractionType.ROBLOX_REMOTE_EVENT)));
    }

    public FrontendProjection projectGuildForRobloxClient(GuildKingdom guildKingdom) {
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.ROBLOX, List.of(action("roblox.guild.gui", "Guild", PlatformInteractionType.ROBLOX_GUI_ACTION)));
    }

    public FrontendProjection projectPetitionForRobloxClient(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.ROBLOX, List.of(action("roblox.petition.gui", "Petition", PlatformInteractionType.ROBLOX_GUI_ACTION)));
    }

    private InteractionAction action(String id, String label, PlatformInteractionType type) {
        return new InteractionAction(id, label, Optional.empty(), Set.of(), true, Optional.empty(), type, Map.of());
    }
}
