package org.tavall.control.companion;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface ICompanionDomainGenerated {
    default CompanionRepository getCompanionRepository() {
        return DependencyLoaderAccess.findInstance(CompanionRepository.class);
    }

    default CompanionFactory getCompanionFactory() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionFactory.class)
                .orElseGet(() -> registerCompanionFactory(new CompanionFactory()));
    }

    default CompanionStatsHandler getCompanionStatsHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionStatsHandler.class)
                .orElseGet(() -> registerCompanionStatsHandler(new CompanionStatsHandler()));
    }

    default CompanionSkillHandler getCompanionSkillHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionSkillHandler.class)
                .orElseGet(() -> registerCompanionSkillHandler(new CompanionSkillHandler()));
    }

    default CompanionLevelingHandler getCompanionLevelingHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionLevelingHandler.class)
                .orElseGet(() -> registerCompanionLevelingHandler(new CompanionLevelingHandler()));
    }

    default CompanionTrainingHandler getCompanionTrainingHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionTrainingHandler.class)
                .orElseGet(() -> registerCompanionTrainingHandler(new CompanionTrainingHandler()));
    }

    default WisdomWellHandler getWisdomWellHandler() {
        return DependencyLoaderAccess.findOptionalInstance(WisdomWellHandler.class)
                .orElseGet(() -> registerWisdomWellHandler(new WisdomWellHandler()));
    }

    default CompanionBehaviorEngine getCompanionBehaviorEngine() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionBehaviorEngine.class)
                .orElseGet(() -> registerCompanionBehaviorEngine(new CompanionBehaviorEngine()));
    }

    default CompanionMoraleHandler getCompanionMoraleHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionMoraleHandler.class)
                .orElseGet(() -> registerCompanionMoraleHandler(new CompanionMoraleHandler()));
    }

    default CompanionWallDefenseHandler getCompanionWallDefenseHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionWallDefenseHandler.class)
                .orElseGet(() -> registerCompanionWallDefenseHandler(new CompanionWallDefenseHandler()));
    }

    default CompanionProjectionHandler getCompanionProjectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionProjectionHandler.class)
                .orElseGet(() -> registerCompanionProjectionHandler(new CompanionProjectionHandler()));
    }

    default CompanionHandler getCompanionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionHandler.class)
                .orElseGet(() -> registerCompanionHandler(new CompanionHandler()));
    }

    default CompanionRepository registerCompanionRepository(CompanionRepository companionRepository) {
        DependencyLoaderAccess.registerInstance(CompanionRepository.class, companionRepository);
        return companionRepository;
    }

    default CompanionFactory registerCompanionFactory(CompanionFactory companionFactory) {
        DependencyLoaderAccess.registerInstance(CompanionFactory.class, companionFactory);
        return companionFactory;
    }

    default CompanionStatsHandler registerCompanionStatsHandler(CompanionStatsHandler companionStatsHandler) {
        DependencyLoaderAccess.registerInstance(CompanionStatsHandler.class, companionStatsHandler);
        return companionStatsHandler;
    }

    default CompanionSkillHandler registerCompanionSkillHandler(CompanionSkillHandler companionSkillHandler) {
        DependencyLoaderAccess.registerInstance(CompanionSkillHandler.class, companionSkillHandler);
        return companionSkillHandler;
    }

    default CompanionLevelingHandler registerCompanionLevelingHandler(CompanionLevelingHandler companionLevelingHandler) {
        DependencyLoaderAccess.registerInstance(CompanionLevelingHandler.class, companionLevelingHandler);
        return companionLevelingHandler;
    }

    default CompanionTrainingHandler registerCompanionTrainingHandler(CompanionTrainingHandler companionTrainingHandler) {
        DependencyLoaderAccess.registerInstance(CompanionTrainingHandler.class, companionTrainingHandler);
        return companionTrainingHandler;
    }

    default WisdomWellHandler registerWisdomWellHandler(WisdomWellHandler wisdomWellHandler) {
        DependencyLoaderAccess.registerInstance(WisdomWellHandler.class, wisdomWellHandler);
        return wisdomWellHandler;
    }

    default CompanionBehaviorEngine registerCompanionBehaviorEngine(CompanionBehaviorEngine companionBehaviorEngine) {
        DependencyLoaderAccess.registerInstance(CompanionBehaviorEngine.class, companionBehaviorEngine);
        return companionBehaviorEngine;
    }

    default CompanionMoraleHandler registerCompanionMoraleHandler(CompanionMoraleHandler companionMoraleHandler) {
        DependencyLoaderAccess.registerInstance(CompanionMoraleHandler.class, companionMoraleHandler);
        return companionMoraleHandler;
    }

    default CompanionWallDefenseHandler registerCompanionWallDefenseHandler(CompanionWallDefenseHandler companionWallDefenseHandler) {
        DependencyLoaderAccess.registerInstance(CompanionWallDefenseHandler.class, companionWallDefenseHandler);
        return companionWallDefenseHandler;
    }

    default CompanionProjectionHandler registerCompanionProjectionHandler(CompanionProjectionHandler companionProjectionHandler) {
        DependencyLoaderAccess.registerInstance(CompanionProjectionHandler.class, companionProjectionHandler);
        return companionProjectionHandler;
    }

    default CompanionHandler registerCompanionHandler(CompanionHandler companionHandler) {
        DependencyLoaderAccess.registerInstance(CompanionHandler.class, companionHandler);
        return companionHandler;
    }
}
