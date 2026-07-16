package org.tavall.control.projection;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.asset.GlobalAssetRegistrationHandler;
import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.asset.GlobalAssetType;
import org.tavall.control.asset.InMemoryGlobalAssetRepository;
import org.tavall.control.asset.PlatformAssetVersionRegistrationHandler;
import org.tavall.control.castle.Castle;
import org.tavall.control.castle.CastleCreationHandler;
import org.tavall.control.clock.KingdomClockControlSystem;
import org.tavall.control.castle.InMemoryCastleRepository;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.common.GamePlatform;
import org.tavall.control.guild.GuildActionValidationHandler;
import org.tavall.control.guild.GuildAuthorityTier;
import org.tavall.control.guild.GuildCreationHandler;
import org.tavall.control.guild.GuildKingdom;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.GuildMembershipHandler;
import org.tavall.control.guild.GuildPermissionValidationHandler;
import org.tavall.control.guild.GuildAuthorityTierHandler;
import org.tavall.control.guild.GuildPermission;
import org.tavall.control.guild.InMemoryGuildRepository;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.node.InMemoryResourceNodeRepository;
import org.tavall.control.node.MiddlewareResourceType;
import org.tavall.control.node.ResourceNode;
import org.tavall.control.node.ResourceNodeCreationHandler;
import org.tavall.control.petition.GuildPetitionCreationHandler;
import org.tavall.control.petition.InMemoryPetitionRepository;
import org.tavall.control.petition.Petition;
import org.tavall.control.petition.PetitionType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ProjectionIntegrationTest {
    @Test
    void globalAssetsResolveForEverySupportedPlatformAndFallbackWhenMissing() {
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        GlobalAssetRegistrationHandler assetRegistrationHandler = new GlobalAssetRegistrationHandler(assetRepository);
        PlatformAssetVersionRegistrationHandler versionRegistrationHandler = new PlatformAssetVersionRegistrationHandler(assetRepository);
        Instant now = Instant.parse("2026-04-30T12:20:00Z");

        GlobalAssetId castleAssetId = new GlobalAssetId("castle.basic.level_1");
        assetRegistrationHandler.registerGlobalAsset(castleAssetId, GlobalAssetType.CASTLE, "Basic Castle", Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(castleAssetId, GamePlatform.MINECRAFT, "minecraft:model/castle_basic", 1, Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(castleAssetId, GamePlatform.ROBLOX, "rbxassetid://1001", 1, Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(castleAssetId, GamePlatform.DISCORD, "discord:emoji:castle", 1, Optional.empty(), now);

        GlobalAssetResolutionHandler resolutionHandler = new GlobalAssetResolutionHandler(assetRepository);

        assertEquals("minecraft:model/castle_basic", resolutionHandler.resolveGlobalAssetForPlatform(castleAssetId, GamePlatform.MINECRAFT).platformAssetReference().orElseThrow());
        assertEquals("rbxassetid://1001", resolutionHandler.resolveGlobalAssetForPlatform(castleAssetId, GamePlatform.ROBLOX).platformAssetReference().orElseThrow());
        assertEquals("discord:emoji:castle", resolutionHandler.resolveGlobalAssetForPlatform(castleAssetId, GamePlatform.DISCORD).platformAssetReference().orElseThrow());
        assertTrue(resolutionHandler.resolveGlobalAssetForPlatform(new GlobalAssetId("node.wood.basic"), GamePlatform.DISCORD).platformAssetReference().isEmpty());
    }

    @Test
    void platformProjectionIncludesGlobalAssetPlatformAssetAndActionTypes() {
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        GlobalAssetRegistrationHandler assetRegistrationHandler = new GlobalAssetRegistrationHandler(assetRepository);
        PlatformAssetVersionRegistrationHandler versionRegistrationHandler = new PlatformAssetVersionRegistrationHandler(assetRepository);
        Instant now = Instant.parse("2026-04-30T12:25:00Z");
        GlobalAssetId castleAssetId = new GlobalAssetId("castle.basic.level_1");
        GlobalAssetId nodeAssetId = new GlobalAssetId("node.wood.basic");
        assetRegistrationHandler.registerGlobalAsset(castleAssetId, GlobalAssetType.CASTLE, "Basic Castle", Optional.empty(), now);
        assetRegistrationHandler.registerGlobalAsset(nodeAssetId, GlobalAssetType.RESOURCE_NODE, "Wood Node", Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(castleAssetId, GamePlatform.MINECRAFT, "minecraft:model/castle_basic", 1, Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(castleAssetId, GamePlatform.ROBLOX, "rbxassetid://1001", 1, Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(castleAssetId, GamePlatform.DISCORD, "discord:emoji:castle", 1, Optional.empty(), now);
        versionRegistrationHandler.registerPlatformAssetVersion(nodeAssetId, GamePlatform.ROBLOX, "rbxassetid://2001", 1, Optional.empty(), now);

        FrontendProjectionHandler projectionHandler = new FrontendProjectionHandler(new GlobalAssetProjectionHandler(new GlobalAssetResolutionHandler(assetRepository)));
        MinecraftProjectionHandler minecraftProjectionHandler = new MinecraftProjectionHandler(projectionHandler);
        RobloxProjectionHandler robloxProjectionHandler = new RobloxProjectionHandler(projectionHandler);
        DiscordProjectionHandler discordProjectionHandler = new DiscordProjectionHandler(projectionHandler);

        UniversalPlayerId owner = UniversalPlayerId.random();
        Castle castle = new CastleCreationHandler(new InMemoryCastleRepository())
                .createCastleForUniversalPlayer(owner, Optional.empty(), new CanonicalLocation("world", 1.0d, 64.0d, 2.0d));
        ResourceNode node = new ResourceNodeCreationHandler(new InMemoryResourceNodeRepository())
                .createResourceNode(MiddlewareResourceType.WOOD, new CanonicalLocation("world", 3.0d, 64.0d, 4.0d), Optional.empty(), Optional.of(owner), 10);

        assertEquals("castle.basic.level_1", minecraftProjectionHandler.projectCastleForMinecraftClient(castle).globalAssetId().value());
        assertEquals("rbxassetid://2001", robloxProjectionHandler.projectResourceNodeForRobloxClient(node).platformAssetReference().orElseThrow());
        FrontendProjection robloxNode = robloxProjectionHandler.projectResourceNodeForRobloxClient(node);
        assertEquals(PlatformInteractionType.ROBLOX_REMOTE_EVENT, robloxNode.interactionActions().getFirst().interactionType());
        assertEquals("rbxassetid://2001", robloxNode.platformAssetReference().orElseThrow());

        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        GuildKingdom guildKingdom = new GuildCreationHandler(guildRepository).createGuildKingdom("Projection Guild", "PRJ", owner, now);
        GuildMemberProfile member = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guildKingdom.guildId(), UniversalPlayerId.random(), now);
        FrontendProjection discordGuild = discordProjectionHandler.projectGuildSummaryForDiscord(guildKingdom, member);
        assertFalse(discordGuild.interactionActions().stream().filter(action -> action.actionId().equals("discord.treasury.view")).findFirst().orElseThrow().enabled());

        new GuildAuthorityTierHandler(guildRepository).setAuthorityTier(guildKingdom.guildId(), member.universalPlayerId(), GuildAuthorityTier.COUNCIL);
        GuildMemberProfile council = guildRepository.findMember(guildKingdom.guildId(), member.universalPlayerId()).orElseThrow();
        FrontendProjection discordGuildForCouncil = discordProjectionHandler.projectGuildSummaryForDiscord(guildKingdom, council);
        assertTrue(discordGuildForCouncil.interactionActions().stream().filter(action -> action.actionId().equals("discord.treasury.view")).findFirst().orElseThrow().enabled());

        Petition petition = new GuildPetitionCreationHandler(new InMemoryPetitionRepository())
                .createPetition(guildKingdom.guildId(), council.universalPlayerId(), council, PetitionType.AUDIT_TREASURY, "Audit treasury", now);
        InteractionAction discordPetitionAction = discordProjectionHandler.projectPetitionForDiscord(petition).interactionActions().getFirst();
        InteractionAction minecraftPetitionAction = minecraftProjectionHandler.projectPetitionForMinecraftClient(petition).interactionActions().getFirst();
        assertEquals(PlatformInteractionType.DISCORD_BUTTON, discordPetitionAction.interactionType());
        assertEquals(GuildAuthorityTier.COUNCIL, discordPetitionAction.requiredTier().orElseThrow());
        assertTrue(discordPetitionAction.requiredPermissions().contains(GuildPermission.CREATE_PETITION));
        assertEquals(PlatformInteractionType.MINECRAFT_COMMAND, minecraftPetitionAction.interactionType());
        assertEquals(GuildAuthorityTier.COUNCIL, minecraftPetitionAction.requiredTier().orElseThrow());
    }

    @Test
    void kingdomClockAndScheduleProjectForMinecraftRobloxAndDiscordWithoutFrontendTimeOwnership() {
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        FrontendProjectionHandler projectionHandler = new FrontendProjectionHandler(new GlobalAssetProjectionHandler(new GlobalAssetResolutionHandler(assetRepository)));
        KingdomClockControlSystem clockSystem = KingdomClockControlSystem.inMemory();

        clockSystem.setTimeOverride("kingdom-1", java.time.LocalTime.of(22, 0));

        FrontendProjection minecraftClock = new MinecraftProjectionHandler(projectionHandler).projectKingdomClockForMinecraftClient(clockSystem.projectClockState("kingdom-1", GamePlatform.MINECRAFT));
        FrontendProjection robloxClock = new RobloxProjectionHandler(projectionHandler).projectKingdomClockForRobloxClient(clockSystem.projectClockState("kingdom-1", GamePlatform.ROBLOX));
        FrontendProjection discordSchedule = new DiscordProjectionHandler(projectionHandler).projectKingdomScheduleSummaryForDiscord(clockSystem.projectScheduleState("kingdom-1", GamePlatform.DISCORD));

        assertEquals(ProjectionObjectType.KINGDOM_CLOCK, minecraftClock.objectType());
        assertEquals("NIGHT", minecraftClock.state());
        assertEquals("plain-java-control-server", robloxClock.metadata().get("canonicalOwner"));
        assertEquals(ProjectionObjectType.KINGDOM_SCHEDULE, discordSchedule.objectType());
        assertTrue(discordSchedule.metadata().containsKey("shopOpenCloseHints"));
    }

    @Test
    void actionAvailabilityDisablesWhenTierIsTooLow() {
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        UniversalPlayerId owner = UniversalPlayerId.random();
        Instant now = Instant.parse("2026-04-30T12:30:00Z");
        GuildKingdom guildKingdom = new GuildCreationHandler(guildRepository).createGuildKingdom("Action Guild", "ACT", owner, now);
        GuildMemberProfile citizen = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guildKingdom.guildId(), UniversalPlayerId.random(), now);

        InteractionActionAvailabilityHandler availabilityHandler = new InteractionActionAvailabilityHandler(
                new GuildActionValidationHandler(new GuildPermissionValidationHandler())
        );

        InteractionAction action = availabilityHandler.actionForRequirement(
                "guild.tax.set",
                "Set Tax",
                guildKingdom,
                citizen,
                org.tavall.control.guild.GuildActionRequirement.of(GuildAuthorityTier.COUNCIL, org.tavall.control.guild.GuildPermission.MANAGE_TAX_POLICY),
                PlatformInteractionType.DISCORD_BUTTON
        );

        assertFalse(action.enabled());
        assertTrue(action.disabledReason().isPresent());
    }
}
