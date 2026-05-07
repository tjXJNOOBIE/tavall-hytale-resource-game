package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.common.HighRiskAction;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GuildGovernanceIntegrationTest {
    @Test
    void authorityTiersExplicitPermissionsAndJobsStaySeparate() {
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        GuildCreationHandler guildCreationHandler = new GuildCreationHandler(guildRepository);
        GuildMembershipHandler membershipHandler = new GuildMembershipHandler(guildRepository);
        GuildAuthorityTierHandler authorityTierHandler = new GuildAuthorityTierHandler(guildRepository);
        GuildActionValidationHandler actionValidationHandler = new GuildActionValidationHandler(new GuildPermissionValidationHandler());
        GuildJobAssignmentHandler jobAssignmentHandler = new GuildJobAssignmentHandler(guildRepository);
        GuildJobBuffCalculationHandler jobBuffCalculationHandler = new GuildJobBuffCalculationHandler();
        Instant now = Instant.parse("2026-04-30T12:40:00Z");

        UniversalPlayerId rulerId = UniversalPlayerId.random();
        GuildKingdom guildKingdom = guildCreationHandler.createGuildKingdom("Governance", "GOV", rulerId, now);
        UniversalPlayerId citizenId = UniversalPlayerId.random();
        GuildMemberProfile citizen = membershipHandler.addPlayerToGuild(guildKingdom.guildId(), citizenId, now);

        GuildActionRequirement inviteRequirement = GuildActionRequirement.of(GuildAuthorityTier.OFFICER, GuildPermission.INVITE_PLAYER);
        assertFalse(actionValidationHandler.validateKingdomActionState(guildKingdom, citizen, inviteRequirement, false));

        GuildMemberProfile officer = authorityTierHandler.setAuthorityTier(guildKingdom.guildId(), citizenId, GuildAuthorityTier.OFFICER);
        assertTrue(actionValidationHandler.validateKingdomActionState(guildKingdom, officer, inviteRequirement, false));

        GuildMemberProfile council = authorityTierHandler.setAuthorityTier(guildKingdom.guildId(), citizenId, GuildAuthorityTier.COUNCIL);
        GuildActionRequirement taxRequirement = GuildActionRequirement.of(GuildAuthorityTier.COUNCIL, GuildPermission.MANAGE_TAX_POLICY);
        assertTrue(actionValidationHandler.validateKingdomActionState(guildKingdom, council, taxRequirement, false));
        assertEquals(0.0d, jobBuffCalculationHandler.calculateJobModifier(council, GuildJobDomain.ECONOMY), 0.0001d);

        GuildMemberProfile treasurer = jobAssignmentHandler.assignJobTitle(guildKingdom.guildId(), citizenId, GuildJobTitle.TREASURER, now.plusSeconds(1));
        assertEquals(0.10d, jobBuffCalculationHandler.calculateJobModifier(treasurer, GuildJobDomain.ECONOMY), 0.0001d);

        GuildActionRequirement transferRequirement = new GuildActionRequirement(
                GuildAuthorityTier.RULER,
                Set.of(GuildPermission.TRANSFER_LEADERSHIP),
                Optional.of(HighRiskAction.GUILD_LEADERSHIP_TRANSFER)
        );
        GuildMemberProfile ruler = guildRepository.findMember(guildKingdom.guildId(), rulerId).orElseThrow();
        assertFalse(actionValidationHandler.validateKingdomActionState(guildKingdom, ruler, transferRequirement, false));
        assertTrue(actionValidationHandler.validateKingdomActionState(guildKingdom, ruler, transferRequirement, true));
    }

    @Test
    void statCalculatorCombinesUpgradeBuffJobAndKingdomState() {
        InMemoryGuildBuffRepository buffRepository = new InMemoryGuildBuffRepository();
        GuildUpgradePurchaseHandler upgradePurchaseHandler = new GuildUpgradePurchaseHandler(buffRepository);
        GuildBuffActivationHandler buffActivationHandler = new GuildBuffActivationHandler(buffRepository);
        GuildStatModifierCalculationHandler calculationHandler = new GuildStatModifierCalculationHandler(new GuildJobBuffCalculationHandler());
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        Instant now = Instant.parse("2026-04-30T12:45:00Z");
        UniversalPlayerId ruler = UniversalPlayerId.random();
        GuildKingdom guildKingdom = new GuildCreationHandler(guildRepository).createGuildKingdom("Buffs", "BUF", ruler, now);
        GuildMemberProfile member = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guildKingdom.guildId(), UniversalPlayerId.random(), now);
        GuildMemberProfile propagandist = new GuildJobAssignmentHandler(guildRepository).assignJobTitle(guildKingdom.guildId(), member.universalPlayerId(), GuildJobTitle.PROPAGANDIST, now);

        upgradePurchaseHandler.purchaseGuildUpgrade(guildKingdom.guildId(), GuildUpgradeType.PROPAGANDA_REACH, 2, now);
        buffActivationHandler.activateGuildBuff(guildKingdom.guildId(), GuildUpgradeType.PROPAGANDA_REACH, 0.20d, java.time.Duration.ofMinutes(10), now);
        buffActivationHandler.activateGuildBuff(guildKingdom.guildId(), GuildUpgradeType.PROPAGANDA_REACH, 0.50d, java.time.Duration.ofSeconds(1), now.minusSeconds(10));

        double finalStat = calculationHandler.calculateFinalStat(new GuildStatCalculationContext(
                100.0d,
                GuildJobDomain.PROPAGANDA,
                propagandist,
                KingdomState.PROTECTED,
                buffRepository.findUpgrades(guildKingdom.guildId()),
                buffRepository.findActiveBuffs(guildKingdom.guildId(), now),
                now
        ));

        assertEquals(147.0d, finalStat, 0.0001d);
    }
}
