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
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendActionCatalog;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendActionDescriptor;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendObjectKind;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MinecraftProjectionHandler implements IProjectionDomain {
    public MinecraftProjectionHandler() {
    }

    public MinecraftProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectCastleForMinecraftClient(Castle castle) {
        return getFrontendProjectionHandler().projectCastle(castle, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceNodeForMinecraftClient(ResourceNode node) {
        return getFrontendProjectionHandler().projectResourceNode(node, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectGuildForMinecraftClient(GuildKingdom guildKingdom) {
        return getFrontendProjectionHandler().projectGuildSummary(guildKingdom, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.GUILD));
    }

    public FrontendProjection projectPetitionForMinecraftClient(Petition petition) {
        return getFrontendProjectionHandler().projectPetition(petition, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectKingdomClockForMinecraftClient(KingdomClockProjection clockProjection) {
        return getFrontendProjectionHandler().projectKingdomClock(clockProjection, GamePlatform.MINECRAFT, List.of());
    }

    public FrontendProjection projectKingdomScheduleForMinecraftClient(KingdomScheduleProjection scheduleProjection) {
        return getFrontendProjectionHandler().projectKingdomSchedule(scheduleProjection, GamePlatform.MINECRAFT, List.of());
    }

    public FrontendProjection projectCitizenPopulationForMinecraftClient(CitizenPopulationProjection citizenProjection) {
        return getFrontendProjectionHandler().projectCitizenPopulation(citizenProjection, GamePlatform.MINECRAFT, List.of());
    }

    public FrontendProjection projectCitizenDisplayAnchorForMinecraftClient(CitizenDisplayAnchorProjection anchorProjection) {
        return getFrontendProjectionHandler().projectCitizenDisplayAnchor(anchorProjection, GamePlatform.MINECRAFT, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return getResourceGameFrontendActionCatalog().actionsFor(ResourceGameFrontendPlatform.MINECRAFT, objectKind).stream()
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
