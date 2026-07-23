package org.tavall.control.economy;

import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.control.security.HighRiskActionChallengeHandler;
import org.tavall.control.security.InMemoryTwoFactorRepository;

public interface EconomyDomain {
    default EconomyRepository getEconomyRepository() {
        return DependencyLoaderAccess.findOptionalInstance(EconomyRepository.class)
                .orElseGet(() -> registerEconomyRepository(new InMemoryEconomyRepository()));
    }

    default GuildTaxPolicyUpdateHandler getGuildTaxPolicyUpdateHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildTaxPolicyUpdateHandler.class)
                .orElseGet(() -> registerGuildTaxPolicyUpdateHandler(new GuildTaxPolicyUpdateHandler()));
    }

    default GuildTaxTransactionLogHandler getGuildTaxTransactionLogHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildTaxTransactionLogHandler.class)
                .orElseGet(() -> registerGuildTaxTransactionLogHandler(new GuildTaxTransactionLogHandler()));
    }

    default GuildTaxCollectionHandler getGuildTaxCollectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildTaxCollectionHandler.class)
                .orElseGet(() -> registerGuildTaxCollectionHandler(new GuildTaxCollectionHandler()));
    }

    default GuildTreasuryBalanceHandler getGuildTreasuryBalanceHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildTreasuryBalanceHandler.class)
                .orElseGet(() -> registerGuildTreasuryBalanceHandler(new GuildTreasuryBalanceHandler()));
    }

    default GuildTreasurySpendHandler getGuildTreasurySpendHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildTreasurySpendHandler.class)
                .orElseGet(() -> registerGuildTreasurySpendHandler(new GuildTreasurySpendHandler()));
    }

    default HighRiskActionChallengeHandler getHighRiskActionChallengeHandler() {
        return DependencyLoaderAccess.findOptionalInstance(HighRiskActionChallengeHandler.class)
                .orElseGet(() -> registerHighRiskActionChallengeHandler(new HighRiskActionChallengeHandler(new InMemoryTwoFactorRepository())));
    }

    default EconomyRepository registerEconomyRepository(EconomyRepository economyRepository) {
        DependencyLoaderAccess.registerInstance(EconomyRepository.class, economyRepository);
        return economyRepository;
    }

    default GuildTaxPolicyUpdateHandler registerGuildTaxPolicyUpdateHandler(GuildTaxPolicyUpdateHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildTaxPolicyUpdateHandler.class, handler);
        return handler;
    }

    default GuildTaxTransactionLogHandler registerGuildTaxTransactionLogHandler(GuildTaxTransactionLogHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildTaxTransactionLogHandler.class, handler);
        return handler;
    }

    default GuildTaxCollectionHandler registerGuildTaxCollectionHandler(GuildTaxCollectionHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildTaxCollectionHandler.class, handler);
        return handler;
    }

    default GuildTreasuryBalanceHandler registerGuildTreasuryBalanceHandler(GuildTreasuryBalanceHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildTreasuryBalanceHandler.class, handler);
        return handler;
    }

    default GuildTreasurySpendHandler registerGuildTreasurySpendHandler(GuildTreasurySpendHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildTreasurySpendHandler.class, handler);
        return handler;
    }

    default HighRiskActionChallengeHandler registerHighRiskActionChallengeHandler(HighRiskActionChallengeHandler handler) {
        DependencyLoaderAccess.registerInstance(HighRiskActionChallengeHandler.class, handler);
        return handler;
    }
}
