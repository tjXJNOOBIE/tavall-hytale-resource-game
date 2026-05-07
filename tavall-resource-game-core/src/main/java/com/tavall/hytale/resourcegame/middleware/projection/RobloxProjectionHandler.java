package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.castle.Castle;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenDisplayAnchorProjection;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenPopulationProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomScheduleProjection;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.guild.GuildAuthorityTier;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.guild.GuildPermission;
import com.tavall.hytale.resourcegame.middleware.node.ResourceNode;
import com.tavall.hytale.resourcegame.middleware.petition.Petition;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendActionCatalog;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendActionDescriptor;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendObjectKind;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RobloxProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;
    private final ResourceGameFrontendActionCatalog actionCatalog;

    public RobloxProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
        this.actionCatalog = new ResourceGameFrontendActionCatalog();
    }

    public FrontendProjection projectCastleForRobloxClient(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceNodeForRobloxClient(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectGuildForRobloxClient(GuildKingdom guildKingdom) {
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.GUILD));
    }

    public FrontendProjection projectPetitionForRobloxClient(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectKingdomClockForRobloxClient(KingdomClockProjection clockProjection) {
        return frontendProjectionHandler.projectKingdomClock(clockProjection, GamePlatform.ROBLOX, List.of());
    }

    public FrontendProjection projectKingdomScheduleForRobloxClient(KingdomScheduleProjection scheduleProjection) {
        return frontendProjectionHandler.projectKingdomSchedule(scheduleProjection, GamePlatform.ROBLOX, List.of());
    }

    public FrontendProjection projectCitizenPopulationForRobloxClient(CitizenPopulationProjection citizenProjection) {
        return frontendProjectionHandler.projectCitizenPopulation(citizenProjection, GamePlatform.ROBLOX, List.of());
    }

    public FrontendProjection projectCitizenDisplayAnchorForRobloxClient(CitizenDisplayAnchorProjection anchorProjection) {
        return frontendProjectionHandler.projectCitizenDisplayAnchor(anchorProjection, GamePlatform.ROBLOX, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return actionCatalog.actionsFor(ResourceGameFrontendPlatform.ROBLOX, objectKind).stream()
                .map(this::action)
                .toList();
    }

    private InteractionAction action(ResourceGameFrontendActionDescriptor descriptor) {
        return new InteractionAction(
                descriptor.actionId(),
                descriptor.label(),
                descriptor.requiredTier().map(GuildAuthorityTier::valueOf),
                permissions(descriptor),
                true,
                Optional.empty(),
                PlatformInteractionType.valueOf(descriptor.interactionType()),
                Map.of()
        );
    }

    private Set<GuildPermission> permissions(ResourceGameFrontendActionDescriptor descriptor) {
        return Set.copyOf(descriptor.requiredPermissions().stream().map(GuildPermission::valueOf).toList());
    }
}
