package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.castle.Castle;
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

    private List<InteractionAction> actionsFor(ResourceGameFrontendObjectKind objectKind) {
        return actionCatalog.actionsFor(ResourceGameFrontendPlatform.DISCORD, objectKind).stream()
                .map(this::action)
                .toList();
    }

    private InteractionAction action(ResourceGameFrontendActionDescriptor descriptor) {
        return new InteractionAction(
                descriptor.actionId(),
                descriptor.label(),
                Optional.empty(),
                Set.of(),
                true,
                Optional.empty(),
                PlatformInteractionType.valueOf(descriptor.interactionType()),
                Map.of()
        );
    }
}
