package org.tavall.control.projection;

import org.tavall.control.castle.Castle;
import org.tavall.control.citizen.CitizenDisplayAnchorProjection;
import org.tavall.control.citizen.CitizenPopulationProjection;
import org.tavall.control.clock.KingdomClockProjection;
import org.tavall.control.clock.KingdomScheduleProjection;
import org.tavall.control.common.GamePlatform;
import org.tavall.control.guild.GuildAuthorityTier;
import org.tavall.control.guild.GuildKingdom;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.GuildPermission;
import org.tavall.control.node.ResourceNode;
import org.tavall.control.petition.Petition;
import org.tavall.control.petition.PropagandaCampaign;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendActionCatalog;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendActionDescriptor;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendObjectKind;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DiscordProjectionHandler implements ProjectionDomain {
    public DiscordProjectionHandler() {
    }

    public DiscordProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectGuildSummaryForDiscord(GuildKingdom guildKingdom, GuildMemberProfile actor) {
        List<InteractionAction> actions = new ArrayList<>(actionsFor(ResourceGameFrontendObjectKind.GUILD));
        boolean canViewTreasury = actor != null && (actor.explicitPermissions().contains(GuildPermission.VIEW_TREASURY) || actor.authorityTier().atLeast(GuildAuthorityTier.COUNCIL));
        actions.add(new InteractionAction(
                "discord.treasury.view",
                "View Treasury",
                Optional.of(GuildAuthorityTier.COUNCIL),
                Set.of(GuildPermission.VIEW_TREASURY),
                canViewTreasury,
                canViewTreasury ? Optional.empty() : Optional.of("Treasury requires council authority or VIEW_TREASURY."),
                PlatformInteractionType.DISCORD_BUTTON,
                Map.of()
        ));
        return getFrontendProjectionHandler().projectGuildSummary(guildKingdom, GamePlatform.DISCORD, actions);
    }

    public FrontendProjection projectPetitionForDiscord(Petition petition) {
        return getFrontendProjectionHandler().projectPetition(petition, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectPropagandaCampaignForDiscord(PropagandaCampaign campaign) {
        return getFrontendProjectionHandler().projectPropagandaCampaign(campaign, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.PROPAGANDA_CAMPAIGN));
    }

    public FrontendProjection projectCastleSummaryForDiscord(Castle castle) {
        return getFrontendProjectionHandler().projectCastle(castle, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceSummaryForDiscord(ResourceNode node) {
        return getFrontendProjectionHandler().projectResourceNode(node, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectKingdomClockSummaryForDiscord(KingdomClockProjection clockProjection) {
        return getFrontendProjectionHandler().projectKingdomClock(clockProjection, GamePlatform.DISCORD, List.of());
    }

    public FrontendProjection projectKingdomScheduleSummaryForDiscord(KingdomScheduleProjection scheduleProjection) {
        return getFrontendProjectionHandler().projectKingdomSchedule(scheduleProjection, GamePlatform.DISCORD, List.of());
    }

    public FrontendProjection projectCitizenPopulationSummaryForDiscord(CitizenPopulationProjection citizenProjection) {
        return getFrontendProjectionHandler().projectCitizenPopulation(citizenProjection, GamePlatform.DISCORD, List.of());
    }

    public FrontendProjection projectCitizenDisplayAnchorForDiscord(CitizenDisplayAnchorProjection anchorProjection) {
        return getFrontendProjectionHandler().projectCitizenDisplayAnchor(anchorProjection, GamePlatform.DISCORD, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return getResourceGameFrontendActionCatalog().actionsFor(ResourceGameFrontendPlatform.DISCORD, objectKind).stream()
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
