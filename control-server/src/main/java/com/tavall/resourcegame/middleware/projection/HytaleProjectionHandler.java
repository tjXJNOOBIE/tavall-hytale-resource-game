package org.tavall.control.projection;

import org.tavall.control.castle.Castle;
import org.tavall.control.citizen.CitizenDisplayAnchorProjection;
import org.tavall.control.citizen.CitizenPopulationProjection;
import org.tavall.control.clock.KingdomClockProjection;
import org.tavall.control.clock.KingdomScheduleProjection;
import org.tavall.control.common.GamePlatform;
import org.tavall.control.guild.GuildAuthorityTier;
import org.tavall.control.guild.GuildKingdom;
import org.tavall.control.guild.GuildPermission;
import org.tavall.control.node.ResourceNode;
import org.tavall.control.petition.Petition;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendActionCatalog;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendActionDescriptor;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendObjectKind;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;

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
