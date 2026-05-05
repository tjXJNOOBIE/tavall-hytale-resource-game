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

public final class MinecraftProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;
    private final ResourceGameFrontendActionCatalog actionCatalog;

    public MinecraftProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
        this.actionCatalog = new ResourceGameFrontendActionCatalog();
    }

    public FrontendProjection projectCastleForMinecraftClient(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceNodeForMinecraftClient(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectGuildForMinecraftClient(GuildKingdom guildKingdom) {
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.GUILD));
    }

    public FrontendProjection projectPetitionForMinecraftClient(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.MINECRAFT, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectKingdomClockForMinecraftClient(KingdomClockProjection clockProjection) {
        return frontendProjectionHandler.projectKingdomClock(clockProjection, GamePlatform.MINECRAFT, List.of());
    }

    public FrontendProjection projectKingdomScheduleForMinecraftClient(KingdomScheduleProjection scheduleProjection) {
        return frontendProjectionHandler.projectKingdomSchedule(scheduleProjection, GamePlatform.MINECRAFT, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return actionCatalog.actionsFor(ResourceGameFrontendPlatform.MINECRAFT, objectKind).stream()
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
