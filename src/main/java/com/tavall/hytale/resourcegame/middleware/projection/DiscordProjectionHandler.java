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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DiscordProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public DiscordProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectGuildSummaryForDiscord(GuildKingdom guildKingdom, GuildMemberProfile actor) {
        List<InteractionAction> actions = new ArrayList<>();
        actions.add(action("discord.guild.view", "View Guild", PlatformInteractionType.DISCORD_SLASH_COMMAND));
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
        return frontendProjectionHandler.projectPetition(petition, GamePlatform.DISCORD, List.of(action("discord.petition.fund", "Fund Petition", PlatformInteractionType.DISCORD_BUTTON)));
    }

    public FrontendProjection projectPropagandaCampaignForDiscord(PropagandaCampaign campaign) {
        return frontendProjectionHandler.projectPropagandaCampaign(campaign, GamePlatform.DISCORD, List.of(action("discord.propaganda.summary", "Campaign", PlatformInteractionType.DISCORD_EMBED_ACTION)));
    }

    public FrontendProjection projectCastleSummaryForDiscord(Castle castle) {
        return frontendProjectionHandler.projectCastle(castle, GamePlatform.DISCORD, List.of(action("discord.castle.view", "Castle", PlatformInteractionType.DISCORD_SLASH_COMMAND)));
    }

    public FrontendProjection projectResourceSummaryForDiscord(ResourceNode node) {
        return frontendProjectionHandler.projectResourceNode(node, GamePlatform.DISCORD, List.of(action("discord.resource.view", "Resource", PlatformInteractionType.DISCORD_SELECT_MENU)));
    }

    private InteractionAction action(String id, String label, PlatformInteractionType type) {
        return new InteractionAction(id, label, Optional.empty(), Set.of(), true, Optional.empty(), type, Map.of());
    }
}
