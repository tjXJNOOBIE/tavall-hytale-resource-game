package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.clock.KingdomClockControlSystem;

public interface ICitizenDomainGenerated {
    default CitizenRepository getCitizenRepository() {
        return DependencyLoaderAccess.findInstance(CitizenRepository.class);
    }

    default CitizenSummaryCacheRepository getCitizenSummaryCacheRepository() {
        return DependencyLoaderAccess.findInstance(CitizenSummaryCacheRepository.class);
    }

    default CitizenAgingConfigRepository getCitizenAgingConfigRepository() {
        return DependencyLoaderAccess.findInstance(CitizenAgingConfigRepository.class);
    }

    default CitizenAgingConfig getCitizenAgingConfig() {
        return getCitizenAgingConfigRepository().current();
    }

    default CitizenAgeStageMappingHandler getCitizenAgeStageMappingHandler() {
        return DependencyLoaderAccess.findInstance(CitizenAgeStageMappingHandler.class);
    }

    default CitizenAgingCalculationHandler getCitizenAgingCalculationHandler() {
        return DependencyLoaderAccess.findInstance(CitizenAgingCalculationHandler.class);
    }

    default CitizenCacheInvalidationHandler getCitizenCacheInvalidationHandler() {
        return DependencyLoaderAccess.findInstance(CitizenCacheInvalidationHandler.class);
    }

    default CitizenLifeStageEligibilityHandler getCitizenLifeStageEligibilityHandler() {
        return DependencyLoaderAccess.findInstance(CitizenLifeStageEligibilityHandler.class);
    }

    default CitizenJobEligibilityHandler getCitizenJobEligibilityHandler() {
        return DependencyLoaderAccess.findInstance(CitizenJobEligibilityHandler.class);
    }

    default CitizenTrainingEligibilityHandler getCitizenTrainingEligibilityHandler() {
        return DependencyLoaderAccess.findInstance(CitizenTrainingEligibilityHandler.class);
    }

    default CitizenClockIntegrationHandler getCitizenClockIntegrationHandler() {
        return DependencyLoaderAccess.findInstance(CitizenClockIntegrationHandler.class);
    }

    default CitizenFoodEffectHandler getCitizenFoodEffectHandler() {
        return DependencyLoaderAccess.findInstance(CitizenFoodEffectHandler.class);
    }

    default CitizenMoraleEffectHandler getCitizenMoraleEffectHandler() {
        return DependencyLoaderAccess.findInstance(CitizenMoraleEffectHandler.class);
    }

    default CitizenHousingEffectHandler getCitizenHousingEffectHandler() {
        return DependencyLoaderAccess.findInstance(CitizenHousingEffectHandler.class);
    }

    default CitizenAggregationCalculationHandler getCitizenAggregationCalculationHandler() {
        return DependencyLoaderAccess.findInstance(CitizenAggregationCalculationHandler.class);
    }

    default CitizenCreationHandler getCitizenCreationHandler() {
        return DependencyLoaderAccess.findInstance(CitizenCreationHandler.class);
    }

    default CitizenReadHandler getCitizenReadHandler() {
        return DependencyLoaderAccess.findInstance(CitizenReadHandler.class);
    }

    default CitizenJobAssignmentHandler getCitizenJobAssignmentHandler() {
        return DependencyLoaderAccess.findInstance(CitizenJobAssignmentHandler.class);
    }

    default CitizenTrainingStartHandler getCitizenTrainingStartHandler() {
        return DependencyLoaderAccess.findInstance(CitizenTrainingStartHandler.class);
    }

    default CitizenTroopPromotionHandler getCitizenTroopPromotionHandler() {
        return DependencyLoaderAccess.findInstance(CitizenTroopPromotionHandler.class);
    }

    default CitizenTroopDemotionHandler getCitizenTroopDemotionHandler() {
        return DependencyLoaderAccess.findInstance(CitizenTroopDemotionHandler.class);
    }

    default CitizenConditionUpdateHandler getCitizenConditionUpdateHandler() {
        return DependencyLoaderAccess.findInstance(CitizenConditionUpdateHandler.class);
    }

    default CitizenAgingMaintenanceHandler getCitizenAgingMaintenanceHandler() {
        return DependencyLoaderAccess.findInstance(CitizenAgingMaintenanceHandler.class);
    }

    default CitizenSummaryCacheRefreshHandler getCitizenSummaryCacheRefreshHandler() {
        return DependencyLoaderAccess.findInstance(CitizenSummaryCacheRefreshHandler.class);
    }

    default CitizenProjectionHandler getCitizenProjectionHandler() {
        return DependencyLoaderAccess.findInstance(CitizenProjectionHandler.class);
    }

    default CitizenControlSystem getCitizenControlSystem() {
        return DependencyLoaderAccess.findInstance(CitizenControlSystem.class);
    }

    default KingdomClockControlSystem getKingdomClockControlSystem() {
        return DependencyLoaderAccess.findInstance(KingdomClockControlSystem.class);
    }

    default <T> T registerCitizenDependency(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
        return instance;
    }
}
