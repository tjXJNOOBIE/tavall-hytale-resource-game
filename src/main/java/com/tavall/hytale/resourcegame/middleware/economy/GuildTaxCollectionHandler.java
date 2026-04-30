package com.tavall.hytale.resourcegame.middleware.economy;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.node.MiddlewareResourceType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class GuildTaxCollectionHandler {
    private final EconomyRepository economyRepository;
    private final GuildJobBuffCalculationHandler guildJobBuffCalculationHandler;

    public GuildTaxCollectionHandler(EconomyRepository economyRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        this.economyRepository = economyRepository;
        this.guildJobBuffCalculationHandler = guildJobBuffCalculationHandler;
    }

    public GuildTaxCollectionResult collectResourceProductionTax(
            GuildId guildId,
            UniversalPlayerId producerId,
            MiddlewareResourceType resourceType,
            int producedAmount,
            GuildMemberProfile actor,
            Instant now
    ) {
        TaxPolicy policy = economyRepository.findTaxPolicy(guildId)
                .orElseGet(() -> TaxPolicy.resourceDefault(producerId, now));
        GuildTreasury treasury = economyRepository.findTreasury(guildId)
                .orElseGet(() -> new GuildTreasury(guildId, 0L, Map.of()));
        if (policy.exemptions().contains(producerId)) {
            return new GuildTaxCollectionResult(producedAmount, 0, treasury, null);
        }
        int tax = (int) Math.floor(producedAmount * policy.rateFor(resourceType));
        if (tax < policy.minimumContribution() && producedAmount >= policy.minimumContribution()) {
            tax = policy.minimumContribution();
        }
        if (policy.maxContributionCap() > 0) {
            tax = Math.min(tax, policy.maxContributionCap());
        }
        double treasurerModifier = guildJobBuffCalculationHandler.calculateJobModifier(actor, GuildJobDomain.ECONOMY);
        int treasuryAmount = (int) Math.floor(tax * (1.0d + treasurerModifier));
        GuildTreasury updatedTreasury = economyRepository.saveTreasury(treasury.withAddedResource(resourceType, treasuryAmount));
        GuildTaxTransaction transaction = new GuildTaxTransaction(
                UUID.randomUUID(),
                guildId,
                producerId,
                Optional.of(resourceType),
                Optional.empty(),
                Optional.of(treasuryAmount),
                "RESOURCE_PRODUCTION_TAX",
                now,
                Map.of("baseTax", Integer.toString(tax))
        );
        economyRepository.saveTaxTransaction(transaction);
        return new GuildTaxCollectionResult(producedAmount - tax, treasuryAmount, updatedTreasury, transaction);
    }
}
