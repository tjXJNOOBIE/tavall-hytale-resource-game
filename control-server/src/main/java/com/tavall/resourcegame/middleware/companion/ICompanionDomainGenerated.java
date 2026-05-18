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

    default CompanionStatsService getCompanionStatsService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionStatsService.class)
                .orElseGet(() -> registerCompanionStatsService(new CompanionStatsService()));
    }

    default CompanionSkillService getCompanionSkillService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionSkillService.class)
                .orElseGet(() -> registerCompanionSkillService(new CompanionSkillService()));
    }

    default CompanionLevelingService getCompanionLevelingService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionLevelingService.class)
                .orElseGet(() -> registerCompanionLevelingService(new CompanionLevelingService()));
    }

    default CompanionTrainingService getCompanionTrainingService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionTrainingService.class)
                .orElseGet(() -> registerCompanionTrainingService(new CompanionTrainingService()));
    }

    default WisdomWellService getWisdomWellService() {
        return DependencyLoaderAccess.findOptionalInstance(WisdomWellService.class)
                .orElseGet(() -> registerWisdomWellService(new WisdomWellService()));
    }

    default CompanionBehaviorEngine getCompanionBehaviorEngine() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionBehaviorEngine.class)
                .orElseGet(() -> registerCompanionBehaviorEngine(new CompanionBehaviorEngine()));
    }

    default CompanionMoraleService getCompanionMoraleService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionMoraleService.class)
                .orElseGet(() -> registerCompanionMoraleService(new CompanionMoraleService()));
    }

    default CompanionWallDefenseService getCompanionWallDefenseService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionWallDefenseService.class)
                .orElseGet(() -> registerCompanionWallDefenseService(new CompanionWallDefenseService()));
    }

    default CompanionProjectionHandler getCompanionProjectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionProjectionHandler.class)
                .orElseGet(() -> registerCompanionProjectionHandler(new CompanionProjectionHandler()));
    }

    default CompanionService getCompanionService() {
        return DependencyLoaderAccess.findOptionalInstance(CompanionService.class)
                .orElseGet(() -> registerCompanionService(new CompanionService()));
    }

    default CompanionRepository registerCompanionRepository(CompanionRepository companionRepository) {
        DependencyLoaderAccess.registerInstance(CompanionRepository.class, companionRepository);
        return companionRepository;
    }

    default CompanionFactory registerCompanionFactory(CompanionFactory companionFactory) {
        DependencyLoaderAccess.registerInstance(CompanionFactory.class, companionFactory);
        return companionFactory;
    }

    default CompanionStatsService registerCompanionStatsService(CompanionStatsService companionStatsService) {
        DependencyLoaderAccess.registerInstance(CompanionStatsService.class, companionStatsService);
        return companionStatsService;
    }

    default CompanionSkillService registerCompanionSkillService(CompanionSkillService companionSkillService) {
        DependencyLoaderAccess.registerInstance(CompanionSkillService.class, companionSkillService);
        return companionSkillService;
    }

    default CompanionLevelingService registerCompanionLevelingService(CompanionLevelingService companionLevelingService) {
        DependencyLoaderAccess.registerInstance(CompanionLevelingService.class, companionLevelingService);
        return companionLevelingService;
    }

    default CompanionTrainingService registerCompanionTrainingService(CompanionTrainingService companionTrainingService) {
        DependencyLoaderAccess.registerInstance(CompanionTrainingService.class, companionTrainingService);
        return companionTrainingService;
    }

    default WisdomWellService registerWisdomWellService(WisdomWellService wisdomWellService) {
        DependencyLoaderAccess.registerInstance(WisdomWellService.class, wisdomWellService);
        return wisdomWellService;
    }

    default CompanionBehaviorEngine registerCompanionBehaviorEngine(CompanionBehaviorEngine companionBehaviorEngine) {
        DependencyLoaderAccess.registerInstance(CompanionBehaviorEngine.class, companionBehaviorEngine);
        return companionBehaviorEngine;
    }

    default CompanionMoraleService registerCompanionMoraleService(CompanionMoraleService companionMoraleService) {
        DependencyLoaderAccess.registerInstance(CompanionMoraleService.class, companionMoraleService);
        return companionMoraleService;
    }

    default CompanionWallDefenseService registerCompanionWallDefenseService(CompanionWallDefenseService companionWallDefenseService) {
        DependencyLoaderAccess.registerInstance(CompanionWallDefenseService.class, companionWallDefenseService);
        return companionWallDefenseService;
    }

    default CompanionProjectionHandler registerCompanionProjectionHandler(CompanionProjectionHandler companionProjectionHandler) {
        DependencyLoaderAccess.registerInstance(CompanionProjectionHandler.class, companionProjectionHandler);
        return companionProjectionHandler;
    }

    default CompanionService registerCompanionService(CompanionService companionService) {
        DependencyLoaderAccess.registerInstance(CompanionService.class, companionService);
        return companionService;
    }
}
