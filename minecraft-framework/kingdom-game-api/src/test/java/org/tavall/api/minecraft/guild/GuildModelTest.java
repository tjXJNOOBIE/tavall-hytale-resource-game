package org.tavall.api.minecraft.guild;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GuildModelTest {
    @Test
    void ranksAndPermissionsMatchTheTierTable() {
        assertTrue(GuildRank.TIER_V.atLeast(GuildRank.TIER_III));
        assertFalse(GuildRank.TIER_II.atLeast(GuildRank.TIER_III));
        assertEquals(GuildRank.TIER_II, GuildPermission.INVITE_PLAYER.minimumRank());
        assertEquals(GuildRank.TIER_IV, GuildPermission.MANAGE_TAXES.minimumRank());
        assertEquals(GuildRank.TIER_V, GuildPermission.TRANSFER_OWNERSHIP.minimumRank());
    }

    @Test
    void rolePolicyAndBuildingUnlocksAreStable() {
        assertEquals(1, GuildRoleSlotPolicy.calculateSlotsForLevel(1));
        assertEquals(2, GuildRoleSlotPolicy.calculateSlotsForLevel(5));
        assertEquals(3, GuildRoleSlotPolicy.calculateSlotsForLevel(10));
        assertEquals(5, GuildRoleSlotPolicy.calculateSlotsForLevel(20));
        assertEquals(8, GuildRoleSlotPolicy.calculateSlotsForLevel(30));
        assertEquals(GuildRank.TIER_IV, GuildRole.WAR_CAPTAIN.minimumRank());
        assertEquals(GuildRank.TIER_II, GuildRole.SCOUTMASTER.minimumRank());
        assertTrue(GuildBuildingType.WAR_ROOM.unlockedRoles().contains(GuildRole.WAR_CAPTAIN));
        assertTrue(GuildBuildingType.MEDICAL_HALL.unlockedRoles().contains(GuildRole.QUARTERMASTER));
        assertTrue(GuildRole.REGENT.assignedOnly());
        assertEquals(GuildRank.TIER_V, GuildRole.REGENT.minimumRank());
    }

    @Test
    void guildRecordsNormalizeInputsAndExposeDerivedValues() {
        GuildId guildId = GuildId.of("aurora");
        PlayerGuildData member = new PlayerGuildDataBuilder()
                .guildId(guildId)
                .universalPlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .rank(GuildRank.TIER_III)
                .explicitPermissions(Set.of(GuildPermission.INVITE_PLAYER))
                .roles(Set.of(GuildRole.ARCHITECT))
                .joinedAt(Instant.parse("2026-05-22T20:00:00Z"))
                .metadata(Map.of("note", "founder"))
                .build();
        GuildBankData bank = new GuildBankDataBuilder()
                .guildId(guildId)
                .coinBalance(500L)
                .resourceBalances(Map.of(GuildResourceType.WOOD, 120, GuildResourceType.IRON, 40))
                .updatedAt(Instant.parse("2026-05-22T20:00:01Z"))
                .metadata(Map.of("vault", "primary"))
                .build();
        GuildMetaData guild = new GuildMetaDataBuilder()
                .guildId(guildId)
                .name("Aurora")
                .tag("AUR")
                .motto("Build the skyward city")
                .ownerPlayerId(member.universalPlayerId())
                .state(GuildState.ACTIVE)
                .guildLevel(20)
                .createdAt(Instant.parse("2026-05-22T20:00:00Z"))
                .updatedAt(Instant.parse("2026-05-22T20:00:01Z"))
                .settings(new GuildSettingsBuilder().publicGuild(true).inviteOnly(false).metadata(Map.of("realm", "north")).build())
                .bank(bank)
                .buildings(Set.of(GuildBuildingType.WAR_ROOM, GuildBuildingType.TREASURY))
                .members(Map.of(member.universalPlayerId(), member))
                .rankPermissions(new EnumMap<>(Map.of(
                        GuildRank.TIER_II, Set.of(GuildPermission.INVITE_PLAYER),
                        GuildRank.TIER_IV, Set.of(GuildPermission.MANAGE_TAXES)
                )))
                .explicitPermissionExpansionAllowed(true)
                .metadata(Map.of("realm", "north"))
                .build();

        assertEquals(5, guild.roleSlotCapacity());
        assertEquals(1, guild.memberCount());
        assertTrue(guild.isPublicGuild());
        assertTrue(guild.unlockedRoles().contains(GuildRole.WAR_CAPTAIN));
        assertTrue(guild.hasMember(member.universalPlayerId()));
    }

    @Test
    void appendOnlyLedgersCopyTheirCollections() {
        GuildId guildId = GuildId.of("aurora");
        GuildChatMessage message = new GuildChatMessageBuilder()
                .messageId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
                .guildId(guildId)
                .senderUniversalPlayerId(UUID.fromString("00000000-0000-0000-0000-000000000011"))
                .channel("guild")
                .minimumVisibleRank(GuildRank.TIER_II)
                .content("hello guild")
                .createdAt(Instant.parse("2026-05-22T20:00:05Z"))
                .metadata(Map.of("mood", "high"))
                .build();
        GuildContributionEvent contribution = new GuildContributionEventBuilder()
                .contributionId(UUID.fromString("00000000-0000-0000-0000-000000000012"))
                .guildId(guildId)
                .universalPlayerId(UUID.fromString("00000000-0000-0000-0000-000000000011"))
                .sourceType("tax")
                .points(25)
                .coinContribution(10)
                .resourceContribution(Map.of(GuildResourceType.WOOD, 3))
                .createdAt(Instant.parse("2026-05-22T20:00:06Z"))
                .metadata(Map.of("season", "spring"))
                .build();
        GuildLeaderboardSnapshot snapshot = new GuildLeaderboardSnapshot(
                "contribution",
                "weekly",
                Instant.parse("2026-05-22T20:00:07Z"),
                List.of(new GuildLeaderboardRow(guildId, "Aurora", "AUR", 100, 500, 12, Instant.parse("2026-05-22T20:00:07Z")))
        );

        assertEquals("guild", message.channel());
        assertEquals(25, contribution.points());
        assertEquals(1, snapshot.rows().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.rows().add(null));
    }

    @Test
    void guildCreationBuilderBuildsAValidGuildSnapshot() {
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        GuildMetaData guild = new GuildCreationBuilder()
                .creator(creatorId)
                .guildName("Skylight")
                .tag("SKY")
                .motto("Reach higher")
                .publicGuild(true)
                .inviteOnly(false)
                .startingCoins(250L)
                .metadata(Map.of("realm", "east"))
                .build();

        assertEquals(creatorId, guild.ownerPlayerId());
        assertEquals("Skylight", guild.name());
        assertEquals("SKY", guild.tag());
        assertEquals(250L, guild.bank().coinBalance());
        assertTrue(guild.isPublicGuild());
        assertTrue(guild.hasMember(creatorId));
    }
}
