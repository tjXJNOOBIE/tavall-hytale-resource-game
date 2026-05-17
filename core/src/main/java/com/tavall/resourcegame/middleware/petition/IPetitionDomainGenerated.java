package com.tavall.resourcegame.middleware.petition;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;

public interface IPetitionDomainGenerated {
    default PetitionRepository getPetitionRepository() {
        return DependencyLoaderAccess.findOptionalInstance(PetitionRepository.class)
                .orElseGet(() -> registerPetitionRepository(new InMemoryPetitionRepository()));
    }

    default GuildPetitionCreationHandler getGuildPetitionCreationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildPetitionCreationHandler.class)
                .orElseGet(() -> registerGuildPetitionCreationHandler(new GuildPetitionCreationHandler()));
    }

    default GuildPetitionFundingHandler getGuildPetitionFundingHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildPetitionFundingHandler.class)
                .orElseGet(() -> registerGuildPetitionFundingHandler(new GuildPetitionFundingHandler()));
    }

    default GuildPetitionReachCalculationHandler getGuildPetitionReachCalculationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildPetitionReachCalculationHandler.class)
                .orElseGet(() -> registerGuildPetitionReachCalculationHandler(new GuildPetitionReachCalculationHandler()));
    }

    default GuildPropagandaCampaignHandler getGuildPropagandaCampaignHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildPropagandaCampaignHandler.class)
                .orElseGet(() -> registerGuildPropagandaCampaignHandler(new GuildPropagandaCampaignHandler()));
    }

    default GuildOverthrowFlowHandler getGuildOverthrowFlowHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GuildOverthrowFlowHandler.class)
                .orElseGet(() -> registerGuildOverthrowFlowHandler(new GuildOverthrowFlowHandler()));
    }

    default PetitionRepository registerPetitionRepository(PetitionRepository petitionRepository) {
        DependencyLoaderAccess.registerInstance(PetitionRepository.class, petitionRepository);
        return petitionRepository;
    }

    default GuildPetitionCreationHandler registerGuildPetitionCreationHandler(GuildPetitionCreationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildPetitionCreationHandler.class, handler);
        return handler;
    }

    default GuildPetitionFundingHandler registerGuildPetitionFundingHandler(GuildPetitionFundingHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildPetitionFundingHandler.class, handler);
        return handler;
    }

    default GuildPetitionReachCalculationHandler registerGuildPetitionReachCalculationHandler(GuildPetitionReachCalculationHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildPetitionReachCalculationHandler.class, handler);
        return handler;
    }

    default GuildPropagandaCampaignHandler registerGuildPropagandaCampaignHandler(GuildPropagandaCampaignHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildPropagandaCampaignHandler.class, handler);
        return handler;
    }

    default GuildOverthrowFlowHandler registerGuildOverthrowFlowHandler(GuildOverthrowFlowHandler handler) {
        DependencyLoaderAccess.registerInstance(GuildOverthrowFlowHandler.class, handler);
        return handler;
    }
}
