package org.tavall.control.guild;

import org.tavall.dependency.DependencyLoaderAccess;

public interface GuildDomain {
    default GuildRepository getGuildRepository() {
        return DependencyLoaderAccess.findOptionalInstance(GuildRepository.class)
                .orElseGet(() -> registerGuildRepository(new InMemoryGuildRepository()));
    }

    default GuildBuffRepository getGuildBuffRepository() {
        return DependencyLoaderAccess.findOptionalInstance(GuildBuffRepository.class)
                .orElseGet(() -> registerGuildBuffRepository(new InMemoryGuildBuffRepository()));
    }

    default GuildPermissionValidationHandler getGuildPermissionValidationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildPermissionValidationHandler.class)
                .orElseGet(() -> registerGuildPermissionValidationHandler(new GuildPermissionValidationHandler()));
    }

    default GuildJobBuffCalculationHandler getGuildJobBuffCalculationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildJobBuffCalculationHandler.class)
                .orElseGet(() -> registerGuildJobBuffCalculationHandler(new GuildJobBuffCalculationHandler()));
    }

    default GuildCreationHandler getGuildCreationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildCreationHandler.class)
                .orElseGet(() -> registerGuildCreationHandler(new GuildCreationHandler()));
    }

    default GuildMembershipHandler getGuildMembershipHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildMembershipHandler.class)
                .orElseGet(() -> registerGuildMembershipHandler(new GuildMembershipHandler()));
    }

    default GuildAuthorityTierHandler getGuildAuthorityTierHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildAuthorityTierHandler.class)
                .orElseGet(() -> registerGuildAuthorityTierHandler(new GuildAuthorityTierHandler()));
    }

    default GuildActionValidationHandler getGuildActionValidationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildActionValidationHandler.class)
                .orElseGet(() -> registerGuildActionValidationHandler(new GuildActionValidationHandler()));
    }

    default GuildJobAssignmentHandler getGuildJobAssignmentHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildJobAssignmentHandler.class)
                .orElseGet(() -> registerGuildJobAssignmentHandler(new GuildJobAssignmentHandler()));
    }

    default GuildBuffActivationHandler getGuildBuffActivationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildBuffActivationHandler.class)
                .orElseGet(() -> registerGuildBuffActivationHandler(new GuildBuffActivationHandler()));
    }

    default GuildUpgradePurchaseHandler getGuildUpgradePurchaseHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildUpgradePurchaseHandler.class)
                .orElseGet(() -> registerGuildUpgradePurchaseHandler(new GuildUpgradePurchaseHandler()));
    }

    default GuildStatModifierCalculationHandler getGuildStatModifierCalculationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildStatModifierCalculationHandler.class)
                .orElseGet(() -> registerGuildStatModifierCalculationHandler(new GuildStatModifierCalculationHandler()));
    }

    default GuildDomainActionBuffHandler getGuildDomainActionBuffHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildDomainActionBuffHandler.class)
                .orElseGet(() -> registerGuildDomainActionBuffHandler(new GuildDomainActionBuffHandler()));
    }

    default GuildRepository registerGuildRepository(GuildRepository guildRepository) {
        DependencyLoaderAccess.registerInstance(GuildRepository.class, guildRepository);
        return guildRepository;
    }

    default GuildBuffRepository registerGuildBuffRepository(GuildBuffRepository guildBuffRepository) {
        DependencyLoaderAccess.registerInstance(GuildBuffRepository.class, guildBuffRepository);
        return guildBuffRepository;
    }

    default GuildPermissionValidationHandler registerGuildPermissionValidationHandler(GuildPermissionValidationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildPermissionValidationHandler.class, handler);
        return handler;
    }

    default GuildJobBuffCalculationHandler registerGuildJobBuffCalculationHandler(GuildJobBuffCalculationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildJobBuffCalculationHandler.class, handler);
        return handler;
    }

    default GuildCreationHandler registerGuildCreationHandler(GuildCreationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildCreationHandler.class, handler);
        return handler;
    }

    default GuildMembershipHandler registerGuildMembershipHandler(GuildMembershipHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildMembershipHandler.class, handler);
        return handler;
    }

    default GuildAuthorityTierHandler registerGuildAuthorityTierHandler(GuildAuthorityTierHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildAuthorityTierHandler.class, handler);
        return handler;
    }

    default GuildActionValidationHandler registerGuildActionValidationHandler(GuildActionValidationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildActionValidationHandler.class, handler);
        return handler;
    }

    default GuildJobAssignmentHandler registerGuildJobAssignmentHandler(GuildJobAssignmentHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildJobAssignmentHandler.class, handler);
        return handler;
    }

    default GuildBuffActivationHandler registerGuildBuffActivationHandler(GuildBuffActivationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildBuffActivationHandler.class, handler);
        return handler;
    }

    default GuildUpgradePurchaseHandler registerGuildUpgradePurchaseHandler(GuildUpgradePurchaseHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildUpgradePurchaseHandler.class, handler);
        return handler;
    }

    default GuildStatModifierCalculationHandler registerGuildStatModifierCalculationHandler(GuildStatModifierCalculationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildStatModifierCalculationHandler.class, handler);
        return handler;
    }

    default GuildDomainActionBuffHandler registerGuildDomainActionBuffHandler(GuildDomainActionBuffHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildDomainActionBuffHandler.class, handler);
        return handler;
    }
}
