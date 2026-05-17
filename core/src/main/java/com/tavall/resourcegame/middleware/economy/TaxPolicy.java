package com.tavall.resourcegame.middleware.economy;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.resourcegame.middleware.node.MiddlewareResourceType;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record TaxPolicy(
        double coinTaxRate,
        Map<MiddlewareResourceType, Double> resourceTaxRateByType,
        double generatorTaxRate,
        int minimumContribution,
        int maxContributionCap,
        Set<UniversalPlayerId> exemptions,
        UniversalPlayerId updatedByPlayerId,
        Instant updatedAt
) {
    public TaxPolicy {
        coinTaxRate = clampRate(coinTaxRate);
        EnumMap<MiddlewareResourceType, Double> copy = new EnumMap<>(MiddlewareResourceType.class);
        if (resourceTaxRateByType != null) {
            for (Map.Entry<MiddlewareResourceType, Double> entry : resourceTaxRateByType.entrySet()) {
                copy.put(entry.getKey(), clampRate(entry.getValue() == null ? 0.0d : entry.getValue()));
            }
        }
        resourceTaxRateByType = Map.copyOf(copy);
        generatorTaxRate = clampRate(generatorTaxRate);
        minimumContribution = Math.max(0, minimumContribution);
        maxContributionCap = Math.max(0, maxContributionCap);
        exemptions = exemptions == null ? Set.of() : Set.copyOf(exemptions);
        Objects.requireNonNull(updatedByPlayerId, "updatedByPlayerId");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static TaxPolicy resourceDefault(UniversalPlayerId actor, Instant now) {
        return new TaxPolicy(0.05d, Map.of(), 0.10d, 0, 0, Set.of(), actor, now);
    }

    public double rateFor(MiddlewareResourceType resourceType) {
        return resourceTaxRateByType.getOrDefault(resourceType, generatorTaxRate);
    }

    private static double clampRate(double rate) {
        return Math.max(0.0d, Math.min(rate, 1.0d));
    }
}
