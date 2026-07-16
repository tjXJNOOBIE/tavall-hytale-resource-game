package org.tavall.control.economy;

import org.tavall.control.guild.GuildId;
import org.tavall.control.guild.GuildJobBuffCalculationHandler;
import org.tavall.control.guild.GuildJobDomain;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.GuildDomain;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.node.MiddlewareResourceType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class GuildTaxCollectionHandler implements EconomyDomain, GuildDomain {
    public GuildTaxCollectionHandler() {
    }

    public GuildTaxCollectionHandler(EconomyRepository economyRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerEconomyRepository(economyRepository);
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public GuildTaxCollectionResult collectResourceProductionTax(
            GuildId guildId,
            UniversalPlayerId producerId,
            MiddlewareResourceType resourceType,
            int producedAmount,
            GuildMemberProfile actor,
            Instant now
    ) {
        TaxPolicy policy = getEconomyRepository().findTaxPolicy(guildId)
                .orElseGet(() -> TaxPolicy.resourceDefault(producerId, now));
        GuildTreasury treasury = getEconomyRepository().findTreasury(guildId)
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
        double treasurerModifier = getGuildJobBuffCalculationHandler().calculateJobModifier(actor, GuildJobDomain.ECONOMY);
        int treasuryAmount = (int) Math.floor(tax * (1.0d + treasurerModifier));
        GuildTreasury updatedTreasury = getEconomyRepository().saveTreasury(treasury.withAddedResource(resourceType, treasuryAmount));
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
        getEconomyRepository().saveTaxTransaction(transaction);
        return new GuildTaxCollectionResult(producedAmount - tax, treasuryAmount, updatedTreasury, transaction);
    }
}
