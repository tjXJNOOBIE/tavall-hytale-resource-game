package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.castle.Castle;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenDisplayAnchorProjection;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenPopulationProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomScheduleProjection;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.guild.GuildAuthorityTier;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.guild.GuildPermission;
import com.tavall.hytale.resourcegame.middleware.node.ResourceNode;
import com.tavall.hytale.resourcegame.middleware.petition.Petition;
import com.tavall.hytale.resourcegame.middleware.petition.PropagandaCampaign;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendActionCatalog;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendActionDescriptor;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendObjectKind;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DiscordProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;
    private final ResourceGameFrontendActionCatalog actionCatalog;

    public DiscordProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
        this.actionCatalog = new ResourceGameFrontendActionCatalog();
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
        return frontendProjectionHandler.projectGuildSummary(guildKingdom, GamePlatform.DISCORD, actions);
    }

    public FrontendProjection projectPetitionForDiscord(Petition petition) {
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.PETITION));
    }

    public FrontendProjection projectPropagandaCampaignForDiscord(PropagandaCampaign campaign) {
        return frontendProjectionHandler.projectPropagandaCampaign(campaign, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.PROPAGANDA_CAMPAIGN));
    }

    public FrontendProjection projectCastleSummaryForDiscord(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.CASTLE));
    }

    public FrontendProjection projectResourceSummaryForDiscord(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.DISCORD, actionsFor(ResourceGameFrontendObjectKind.RESOURCE_NODE));
    }

    public FrontendProjection projectKingdomClockSummaryForDiscord(KingdomClockProjection clockProjection) {
        return frontendProjectionHandler.projectKingdomClock(clockProjection, GamePlatform.DISCORD, List.of());
    }

    public FrontendProjection projectKingdomScheduleSummaryForDiscord(KingdomScheduleProjection scheduleProjection) {
        return frontendProjectionHandler.projectKingdomSchedule(scheduleProjection, GamePlatform.DISCORD, List.of());
    }

    public FrontendProjection projectCitizenPopulationSummaryForDiscord(CitizenPopulationProjection citizenProjection) {
        return frontendProjectionHandler.projectCitizenPopulation(citizenProjection, GamePlatform.DISCORD, List.of());
    }

    public FrontendProjection projectCitizenDisplayAnchorForDiscord(CitizenDisplayAnchorProjection anchorProjection) {
        return frontendProjectionHandler.projectCitizenDisplayAnchor(anchorProjection, GamePlatform.DISCORD, List.of());
    }

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return actionCatalog.actionsFor(ResourceGameFrontendPlatform.DISCORD, objectKind).stream()
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
