package com.tavall.resourcegame.middleware.economy;

import com.tavall.resourcegame.middleware.guild.GuildCreationHandler;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.guild.GuildJobAssignmentHandler;
import com.tavall.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.resourcegame.middleware.guild.GuildJobTitle;
import com.tavall.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.resourcegame.middleware.guild.GuildMembershipHandler;
import com.tavall.resourcegame.middleware.guild.InMemoryGuildRepository;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.resourcegame.middleware.node.MiddlewareResourceType;
import com.tavall.resourcegame.middleware.security.HighRiskActionChallengeHandler;
import com.tavall.resourcegame.middleware.security.InMemoryTwoFactorRepository;
import com.tavall.resourcegame.middleware.security.IsolatedSecretCodec;
import com.tavall.resourcegame.middleware.security.TotpCodeGenerator;
import com.tavall.resourcegame.middleware.security.TwoFactorEnrollmentCreated;
import com.tavall.resourcegame.middleware.security.TwoFactorEnrollmentHandler;
import com.tavall.resourcegame.middleware.security.TwoFactorVerificationHandler;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class EconomyIntegrationTest {
    @Test
    void resourceTaxAppliesPolicyCapExemptionAndTreasurerModifier() {
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        InMemoryEconomyRepository economyRepository = new InMemoryEconomyRepository();
        GuildJobBuffCalculationHandler jobBuffCalculationHandler = new GuildJobBuffCalculationHandler();
        GuildTaxCollectionHandler taxCollectionHandler = new GuildTaxCollectionHandler(economyRepository, jobBuffCalculationHandler);
        GuildTaxPolicyUpdateHandler policyUpdateHandler = new GuildTaxPolicyUpdateHandler(economyRepository);
        Instant now = Instant.parse("2026-04-30T12:50:00Z");
        UniversalPlayerId ruler = UniversalPlayerId.random();
        GuildKingdom guildKingdom = new GuildCreationHandler(guildRepository).createGuildKingdom("Taxes", "TAX", ruler, now);
        UniversalPlayerId producer = UniversalPlayerId.random();
        GuildMemberProfile member = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guildKingdom.guildId(), producer, now);
        GuildMemberProfile treasurer = new GuildJobAssignmentHandler(guildRepository).assignJobTitle(guildKingdom.guildId(), producer, GuildJobTitle.TREASURER, now);

        policyUpdateHandler.updateTaxPolicy(guildKingdom.guildId(), new TaxPolicy(
                0.0d,
                Map.of(MiddlewareResourceType.WOOD, 0.20d),
                0.0d,
                0,
                0,
                Set.of(),
                ruler,
                now
        ));

        GuildTaxCollectionResult result = taxCollectionHandler.collectResourceProductionTax(
                guildKingdom.guildId(),
                producer,
                MiddlewareResourceType.WOOD,
                100,
                treasurer,
                now.plusSeconds(1)
        );

        assertEquals(80, result.playerAmount());
        assertEquals(22, result.guildTaxAmount());
        assertEquals(22, result.updatedTreasury().resourceBalances().get(MiddlewareResourceType.WOOD));
        assertEquals(1, economyRepository.findTaxTransactions(guildKingdom.guildId()).size());

        policyUpdateHandler.updateTaxPolicy(guildKingdom.guildId(), new TaxPolicy(
                0.0d,
                Map.of(MiddlewareResourceType.WOOD, 0.50d),
                0.0d,
                0,
                10,
                Set.of(producer),
                ruler,
                now.plusSeconds(2)
        ));
        GuildTaxCollectionResult exemptResult = taxCollectionHandler.collectResourceProductionTax(guildKingdom.guildId(), producer, MiddlewareResourceType.WOOD, 100, member, now.plusSeconds(3));
        assertEquals(100, exemptResult.playerAmount());
        assertEquals(0, exemptResult.guildTaxAmount());

        policyUpdateHandler.updateTaxPolicy(guildKingdom.guildId(), new TaxPolicy(
                0.0d,
                Map.of(MiddlewareResourceType.WOOD, 0.50d),
                0.0d,
                0,
                10,
                Set.of(),
                ruler,
                now.plusSeconds(4)
        ));
        GuildTaxCollectionResult cappedResult = taxCollectionHandler.collectResourceProductionTax(guildKingdom.guildId(), producer, MiddlewareResourceType.WOOD, 100, member, now.plusSeconds(5));
        assertEquals(90, cappedResult.playerAmount());
        assertEquals(10, cappedResult.guildTaxAmount());
    }

    @Test
    void largeTreasurySpendRequiresTwoFactorWhenEnabled() {
        InMemoryEconomyRepository economyRepository = new InMemoryEconomyRepository();
        InMemoryTwoFactorRepository twoFactorRepository = new InMemoryTwoFactorRepository();
        UniversalPlayerId actor = UniversalPlayerId.random();
        GuildId guildId = GuildId.random();
        Instant now = Instant.parse("2026-04-30T12:55:00Z");
        economyRepository.saveTreasury(new GuildTreasury(guildId, 1_000L, Map.of()));
        IsolatedSecretCodec secretCodec = new IsolatedSecretCodec();
        TotpCodeGenerator totpCodeGenerator = new TotpCodeGenerator();
        TwoFactorEnrollmentCreated enrollment = new TwoFactorEnrollmentHandler(twoFactorRepository, secretCodec, new SecureRandom(new byte[]{1}))
                .createPendingTotpEnrollment(actor, now);
        new TwoFactorVerificationHandler(twoFactorRepository, secretCodec, totpCodeGenerator)
                .verifyEnrollment(enrollment.enrollment().enrollmentId(), totpCodeGenerator.generateCode(enrollment.totpSecret(), now), now);

        GuildTreasurySpendHandler spendHandler = new GuildTreasurySpendHandler(
                economyRepository,
                new HighRiskActionChallengeHandler(twoFactorRepository),
                100L
        );

        assertThrows(SecurityException.class, () -> spendHandler.spendCoins(guildId, actor, 200L, false));
        assertEquals(800L, spendHandler.spendCoins(guildId, actor, 200L, true).coinBalance());
    }
}
