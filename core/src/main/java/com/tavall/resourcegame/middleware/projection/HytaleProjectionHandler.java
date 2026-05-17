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

public final class HytaleProjectionHandler implements IProjectionDomain {
    public HytaleProjectionHandler() {
    }

    public HytaleProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectCastleForHytaleClient(Castle castle) {
        return getFrontendProjectionHandler().projectCastle(castle, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceNodeForHytaleClient(ResourceNode node) {
        return getFrontendProjectionHandler().projectResourceNode(node, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectGuildForHytaleClient(GuildKingdom guildKingdom) {
        return getFrontendProjectionHandler().projectGuildSummary(guildKingdom, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.GUILD));
    }

    public FrontendProjection projectPetitionForHytaleClient(Petition petition) {
        return getFrontendProjectionHandler().projectPetition(petition, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectKingdomClockForHytaleClient(KingdomClockProjection clockProjection) {
        return getFrontendProjectionHandler().projectKingdomClock(clockProjection, GamePlatform.HYTALE, List.of());
    }

    public FrontendProjection projectKingdomScheduleForHytaleClient(KingdomScheduleProjection scheduleProjection) {
        return getFrontendProjectionHandler().projectKingdomSchedule(scheduleProjection, GamePlatform.HYTALE, List.of());
    }

    public FrontendProjection projectCitizenPopulationForHytaleClient(CitizenPopulationProjection citizenProjection) {
        return getFrontendProjectionHandler().projectCitizenPopulation(citizenProjection, GamePlatform.HYTALE, List.of());
    }

    public FrontendProjection projectCitizenDisplayAnchorForHytaleClient(CitizenDisplayAnchorProjection anchorProjection) {
        return getFrontendProjectionHandler().projectCitizenDisplayAnchor(anchorProjection, GamePlatform.HYTALE, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return getResourceGameFrontendActionCatalog().actionsFor(ResourceGameFrontendPlatform.HYTALE, objectKind).stream()
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
