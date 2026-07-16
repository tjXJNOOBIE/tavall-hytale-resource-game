package org.tavall.control.guild;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.common.HighRiskAction;
import org.tavall.control.identity.UniversalPlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GuildGovernanceIntegrationTest {
    @BeforeEach
    void clearDependencies() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void authorityTiersExplicitPermissionsAndJobsStaySeparate() {
        GuildDomain guildDomain = new GuildDomain() {
        };
        GuildRepository guildRepository = guildDomain.registerGuildRepository(new InMemoryGuildRepository());
        GuildCreationHandler guildCreationHandler = guildDomain.getGuildCreationHandler();
        GuildMembershipHandler membershipHandler = guildDomain.getGuildMembershipHandler();
        GuildAuthorityTierHandler authorityTierHandler = guildDomain.getGuildAuthorityTierHandler();
        GuildActionValidationHandler actionValidationHandler = guildDomain.getGuildActionValidationHandler();
        GuildJobAssignmentHandler jobAssignmentHandler = guildDomain.getGuildJobAssignmentHandler();
        GuildJobBuffCalculationHandler jobBuffCalculationHandler = guildDomain.getGuildJobBuffCalculationHandler();
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
        GuildDomain guildDomain = new GuildDomain() {
        };
        GuildBuffRepository buffRepository = guildDomain.registerGuildBuffRepository(new InMemoryGuildBuffRepository());
        guildDomain.registerGuildRepository(new InMemoryGuildRepository());
        GuildUpgradePurchaseHandler upgradePurchaseHandler = guildDomain.getGuildUpgradePurchaseHandler();
        GuildBuffActivationHandler buffActivationHandler = guildDomain.getGuildBuffActivationHandler();
        GuildStatModifierCalculationHandler calculationHandler = guildDomain.getGuildStatModifierCalculationHandler();
        Instant now = Instant.parse("2026-04-30T12:45:00Z");
        UniversalPlayerId ruler = UniversalPlayerId.random();
        GuildKingdom guildKingdom = guildDomain.getGuildCreationHandler().createGuildKingdom("Buffs", "BUF", ruler, now);
        GuildMemberProfile member = guildDomain.getGuildMembershipHandler().addPlayerToGuild(guildKingdom.guildId(), UniversalPlayerId.random(), now);
        GuildMemberProfile propagandist = guildDomain.getGuildJobAssignmentHandler().assignJobTitle(guildKingdom.guildId(), member.universalPlayerId(), GuildJobTitle.PROPAGANDIST, now);

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
