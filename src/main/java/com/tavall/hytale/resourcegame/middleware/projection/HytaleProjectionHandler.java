package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.castle.Castle;
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

public final class HytaleProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;
    private final ResourceGameFrontendActionCatalog actionCatalog;

    public HytaleProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
        this.actionCatalog = new ResourceGameFrontendActionCatalog();
    }

    public FrontendProjection projectCastleForHytaleClient(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceNodeForHytaleClient(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectGuildForHytaleClient(GuildKingdom guildKingdom) {
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.GUILD));
    }

    public FrontendProjection projectPetitionForHytaleClient(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.HYTALE, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectKingdomClockForHytaleClient(KingdomClockProjection clockProjection) {
        return frontendProjectionHandler.projectKingdomClock(clockProjection, GamePlatform.HYTALE, List.of());
    }

    public FrontendProjection projectKingdomScheduleForHytaleClient(KingdomScheduleProjection scheduleProjection) {
        return frontendProjectionHandler.projectKingdomSchedule(scheduleProjection, GamePlatform.HYTALE, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return actionCatalog.actionsFor(ResourceGameFrontendPlatform.HYTALE, objectKind).stream()
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
