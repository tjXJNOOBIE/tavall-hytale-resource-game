package org.tavall.api.minecraft.guild;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.dependency.injection.helpers.DependencyInjectorHelper;
import org.tavall.dependency.maps.DependencyMap;
import org.tavall.dependency.composition.IDependencyBundleAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GuildDependencyInjectionTest {
    private static final String GUILD_PACKAGE = "org.tavall.api.minecraft.guild";

    @BeforeEach
    void resetDiState() {
        DependencyMap.getDependencyMap().clear();
    }

    @AfterEach
    void clearDiState() {
        DependencyMap.getDependencyMap().clear();
    }

    @Test
    void scansAndRegistersGuildDependencies() {
        DependencyInjectorHelper<IDependencyInjectableInterface, IDependencyInjectableConcrete> helper =
                new DependencyInjectorHelper<>();
        helper.setBasePackage(GUILD_PACKAGE);
        helper.setupDISystem(getClass().getClassLoader());

        IGuildStateStore stateStore = DependencyLoaderAccess.requireInstance(IGuildStateStore.class);
        GuildCreationHandler creationHandler = (GuildCreationHandler) DependencyLoaderAccess.requireInstance(IGuildCreationHandler.class);
        IGuildMembershipHandler membershipHandler = DependencyLoaderAccess.requireInstance(IGuildMembershipHandler.class);
        IGuildRoleHandler roleHandler = DependencyLoaderAccess.requireInstance(IGuildRoleHandler.class);
        IGuildRankPermissionPolicy rankPermissionPolicy = DependencyLoaderAccess.requireInstance(IGuildRankPermissionPolicy.class);
        IGuildRoleSlotPolicy roleSlotPolicy = DependencyLoaderAccess.requireInstance(IGuildRoleSlotPolicy.class);
        IGuildSettings guildSettings = DependencyLoaderAccess.requireInstance(IGuildSettings.class);
        IGuildPlayerDirectory playerDirectory = DependencyLoaderAccess.requireInstance(IGuildPlayerDirectory.class);
        IGuildCreationBuilder creationBuilder = DependencyLoaderAccess.requireInstance(IGuildCreationBuilder.class);
        IGuildMetaDataBuilder metaDataBuilder = DependencyLoaderAccess.requireInstance(IGuildMetaDataBuilder.class);
        IGuildSettingsBuilder settingsBuilder = DependencyLoaderAccess.requireInstance(IGuildSettingsBuilder.class);
        IPlayerGuildDataBuilder playerGuildDataBuilder = DependencyLoaderAccess.requireInstance(IPlayerGuildDataBuilder.class);
        IGuildBankDataBuilder guildBankDataBuilder = DependencyLoaderAccess.requireInstance(IGuildBankDataBuilder.class);
        IGuildBankTransactionBuilder guildBankTransactionBuilder = DependencyLoaderAccess.requireInstance(IGuildBankTransactionBuilder.class);
        IGuildChatMessageBuilder guildChatMessageBuilder = DependencyLoaderAccess.requireInstance(IGuildChatMessageBuilder.class);
        IGuildContributionEventBuilder guildContributionEventBuilder = DependencyLoaderAccess.requireInstance(IGuildContributionEventBuilder.class);
        IGuildBankHandler bankHandler = DependencyLoaderAccess.requireInstance(IGuildBankHandler.class);
        IGuildChatHandler chatHandler = DependencyLoaderAccess.requireInstance(IGuildChatHandler.class);
        IGuildContributionHandler contributionHandler = DependencyLoaderAccess.requireInstance(IGuildContributionHandler.class);
        IGuildLeaderboardHandler leaderboardHandler = DependencyLoaderAccess.requireInstance(IGuildLeaderboardHandler.class);

        assertNotNull(stateStore);
        assertNotNull(creationHandler);
        assertNotNull(membershipHandler);
        assertNotNull(roleHandler);
        assertNotNull(rankPermissionPolicy);
        assertNotNull(roleSlotPolicy);
        assertNotNull(guildSettings);
        assertNotNull(playerDirectory);
        assertNotNull(bankHandler);
        assertNotNull(chatHandler);
        assertNotNull(contributionHandler);
        assertNotNull(leaderboardHandler);

        IDependencyBundleAccess<IGuildDependencies> bundleAccess = creationHandler;
        IGuildDependencies dependencies = bundleAccess.getDependencies();
        assertNotNull(dependencies);
        assertSame(DependencyLoaderAccess.requireInstance(IGuildDependencies.class), dependencies);
        assertSame(stateStore, dependencies.guildStateStore());
        assertSame(playerDirectory, dependencies.guildPlayerDirectory());
        assertSame(rankPermissionPolicy, dependencies.guildRankPermissionPolicy());
        assertSame(roleSlotPolicy, dependencies.guildRoleSlotPolicy());
        assertSame(creationHandler, dependencies.guildCreationHandler());
        assertSame(membershipHandler, dependencies.guildMembershipHandler());
        assertSame(roleHandler, dependencies.guildRoleHandler());
        assertSame(bankHandler, dependencies.guildBankHandler());
        assertSame(chatHandler, dependencies.guildChatHandler());
        assertSame(contributionHandler, dependencies.guildContributionHandler());
        assertSame(leaderboardHandler, dependencies.guildLeaderboardHandler());

        UUID ownerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID joinerId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        UUID targetId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        GuildMetaData guild = creationHandler.createGuild(ownerId, "Aurora", "AUR", "Build the skyward city");
        assertEquals(ownerId, guild.ownerPlayerId());
        assertTrue(stateStore.findByName("Aurora").isPresent());
        assertEquals(0L, bankHandler.getBank(guild.guildId()).coinBalance());
        assertEquals(1, roleSlotPolicy.slotsForLevel(1));
        assertTrue(rankPermissionPolicy.permissionsFor(GuildRank.TIER_II).contains(GuildPermission.INVITE_PLAYER));
        assertTrue(guildSettings.publicGuild());
        assertSame(creationBuilder, dependencies.guildCreationBuilder());
        assertSame(metaDataBuilder, dependencies.guildMetaDataBuilder());
        assertSame(settingsBuilder, dependencies.guildSettingsBuilder());
        assertSame(playerGuildDataBuilder, dependencies.playerGuildDataBuilder());
        assertSame(guildBankDataBuilder, dependencies.guildBankDataBuilder());
        assertSame(guildBankTransactionBuilder, dependencies.guildBankTransactionBuilder());
        assertSame(guildChatMessageBuilder, dependencies.guildChatMessageBuilder());
        assertSame(guildContributionEventBuilder, dependencies.guildContributionEventBuilder());

        UUID builderCreatorId = UUID.fromString("00000000-0000-0000-0000-00000000000a");
        GuildMetaData builderGuild = creationBuilder
                .creator(builderCreatorId)
                .guildName("Builder Bay")
                .tag("BLD")
                .motto("Built by DI")
                .publicGuild(true)
                .inviteOnly(false)
                .startingCoins(75L)
                .metadata(Map.of("source", "di-test"))
                .build();
        assertEquals(builderCreatorId, builderGuild.ownerPlayerId());
        assertEquals("Builder Bay", builderGuild.name());
        assertEquals(75L, builderGuild.bank().coinBalance());

        GuildMetaData joinedGuild = membershipHandler.joinPublicGuild(joinerId, guild.guildId());
        assertTrue(joinedGuild.hasMember(joinerId));

        guild = membershipHandler.invitePlayer(ownerId, targetId);
        assertEquals(guild, stateStore.requireByGuildId(guild.guildId()));

        GuildMetaData expandedGuild = metaDataBuilder
                .copyOf(guild)
                .guildLevel(5)
                .updatedAt(Instant.parse("2026-05-22T20:00:03Z"))
                .settings(settingsBuilder.publicGuild(true).inviteOnly(false).metadata(Map.of("mode", "test")).build())
                .buildings(Set.of(GuildBuildingType.WAR_ROOM))
                .members(Map.of(
                        ownerId, guild.members().get(ownerId),
                        targetId, playerGuildDataBuilder
                                .guildId(guild.guildId())
                                .universalPlayerId(targetId)
                                .rank(GuildRank.TIER_II)
                                .explicitPermissions(Set.of())
                                .roles(Set.of())
                                .joinedAt(Instant.parse("2026-05-22T20:00:03Z"))
                                .metadata(Map.of("source", "test-member"))
                                .build()
                ))
                .build();
        guild = stateStore.save(expandedGuild);

        guild = roleHandler.assignRole(ownerId, ownerId, GuildRole.WAR_CAPTAIN);
        assertTrue(guild.members().get(ownerId).roles().contains(GuildRole.WAR_CAPTAIN));

        guild = membershipHandler.promoteMember(ownerId, targetId, GuildRank.TIER_III);
        assertEquals(GuildRank.TIER_III, guild.members().get(targetId).rank());

        guild = membershipHandler.kickPlayer(ownerId, targetId);
        assertEquals(guild, stateStore.requireByGuildId(guild.guildId()));
        assertFalse(guild.hasMember(targetId));

        GuildBankTransaction bankTransaction = bankHandler.recordTransaction(
                guildBankTransactionBuilder
                        .transactionId(UUID.fromString("00000000-0000-0000-0000-000000000020"))
                        .guildId(guild.guildId())
                        .actorUniversalPlayerId(ownerId)
                        .transactionType("deposit")
                        .coinDelta(250L)
                        .resourceDelta(java.util.Map.of(GuildResourceType.WOOD, 10))
                        .reason("seed the vault")
                        .createdAt(Instant.parse("2026-05-22T20:00:04Z"))
                        .metadata(java.util.Map.of("source", "test"))
                        .build()
        );
        assertEquals(250L, bankHandler.getBank(guild.guildId()).coinBalance());

        GuildChatMessage message = chatHandler.postMessage(
                guildChatMessageBuilder
                        .messageId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
                        .guildId(guild.guildId())
                        .senderUniversalPlayerId(ownerId)
                        .channel("guild")
                        .minimumVisibleRank(GuildRank.TIER_I)
                        .content("hello guild")
                        .createdAt(Instant.parse("2026-05-22T20:00:05Z"))
                        .metadata(java.util.Map.of("mood", "high"))
                        .build()
        );

        GuildContributionEvent contribution = contributionHandler.recordContribution(
                guildContributionEventBuilder
                        .contributionId(UUID.fromString("00000000-0000-0000-0000-000000000011"))
                        .guildId(guild.guildId())
                        .universalPlayerId(ownerId)
                        .sourceType("tax")
                        .points(25)
                        .coinContribution(10)
                        .resourceContribution(java.util.Map.of(GuildResourceType.WOOD, 3))
                        .createdAt(Instant.parse("2026-05-22T20:00:06Z"))
                        .metadata(java.util.Map.of("season", "spring"))
                        .build()
        );

        GuildLeaderboardSnapshot snapshot = leaderboardHandler.buildSnapshot("guild_contribution", "all_time", 10);

        assertNotNull(bankTransaction);
        assertNotNull(message);
        assertNotNull(contribution);
        assertNotNull(snapshot);
        assertEquals(1, snapshot.rows().size());
        assertEquals(guild.guildId(), snapshot.rows().get(0).guildId());
    }
}


