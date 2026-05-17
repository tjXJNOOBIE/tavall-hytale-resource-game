package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.castle.Castle;
import com.tavall.resourcegame.middleware.citizen.CitizenDisplayAnchorProjection;
import com.tavall.resourcegame.middleware.citizen.CitizenPopulationProjection;
import com.tavall.resourcegame.middleware.clock.KingdomClockProjection;
import com.tavall.resourcegame.middleware.clock.KingdomScheduleProjection;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.guild.GuildAuthorityTier;
import com.tavall.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.resourcegame.middleware.guild.GuildPermission;
import com.tavall.resourcegame.middleware.node.ResourceNode;
import com.tavall.resourcegame.middleware.petition.Petition;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendActionCatalog;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendActionDescriptor;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendObjectKind;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RobloxProjectionHandler implements IProjectionDomain {
    public RobloxProjectionHandler() {
    }

    public RobloxProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectCastleForRobloxClient(Castle castle) {
        return getFrontendProjectionHandler().projectCastle(castle, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceNodeForRobloxClient(ResourceNode node) {
        return getFrontendProjectionHandler().projectResourceNode(node, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectGuildForRobloxClient(GuildKingdom guildKingdom) {
        return getFrontendProjectionHandler().projectGuildSummary(guildKingdom, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.GUILD));
    }

    public FrontendProjection projectPetitionForRobloxClient(Petition petition) {
        return getFrontendProjectionHandler().projectPetition(petition, GamePlatform.ROBLOX, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectKingdomClockForRobloxClient(KingdomClockProjection clockProjection) {
        return getFrontendProjectionHandler().projectKingdomClock(clockProjection, GamePlatform.ROBLOX, List.of());
    }

    public FrontendProjection projectKingdomScheduleForRobloxClient(KingdomScheduleProjection scheduleProjection) {
        return getFrontendProjectionHandler().projectKingdomSchedule(scheduleProjection, GamePlatform.ROBLOX, List.of());
    }

    public FrontendProjection projectCitizenPopulationForRobloxClient(CitizenPopulationProjection citizenProjection) {
        return getFrontendProjectionHandler().projectCitizenPopulation(citizenProjection, GamePlatform.ROBLOX, List.of());
    }

    public FrontendProjection projectCitizenDisplayAnchorForRobloxClient(CitizenDisplayAnchorProjection anchorProjection) {
        return getFrontendProjectionHandler().projectCitizenDisplayAnchor(anchorProjection, GamePlatform.ROBLOX, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return getResourceGameFrontendActionCatalog().actionsFor(ResourceGameFrontendPlatform.ROBLOX, objectKind).stream()
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
