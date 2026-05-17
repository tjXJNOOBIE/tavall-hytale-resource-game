package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.asset.GlobalAssetRepository;
import com.tavall.resourcegame.middleware.asset.PlatformAssetVersionRegistrationHandler;
import com.tavall.resourcegame.middleware.authority.IControlAuthorizationHandler;
import com.tavall.resourcegame.middleware.citizen.CitizenControlSystem;
import com.tavall.resourcegame.middleware.clock.KingdomClockControlSystem;
import com.tavall.resourcegame.middleware.companion.CompanionService;
import com.tavall.resourcegame.middleware.healing.HealingFacilityDefinitionRegistry;
import com.tavall.resourcegame.middleware.healing.HealingInventoryRepository;
import com.tavall.resourcegame.middleware.healing.TroopHealingProgressTickHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingRecipeSelectionHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingRecipeValidationHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingRepository;
import com.tavall.resourcegame.middleware.healing.TroopHealingStartHandler;
import com.tavall.resourcegame.middleware.healing.TroopWoundAssignmentHandler;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBindingRepository;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerAccountRepository;
import com.tavall.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import com.tavall.resourcegame.middleware.troop.TroopRepository;

import java.time.Clock;
import java.util.Optional;

public interface IControlCommandDomainGenerated {
    default ControlCommandRegistry getControlCommandRegistry() {
        return DependencyLoaderAccess.findInstance(ControlCommandRegistry.class);
    }

    default ControlCommandValidationHandler getControlCommandValidationHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandValidationHandler.class);
    }

    default ControlCommandExecutionHandler getControlCommandExecutionHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandExecutionHandler.class);
    }

    default ControlCommandDispatchHandler getControlCommandDispatchHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandDispatchHandler.class);
    }

    default PlatformCommandFanoutHandler getPlatformCommandFanoutHandler() {
        return DependencyLoaderAccess.findInstance(PlatformCommandFanoutHandler.class);
    }

    default PlatformFrontendAdapterRegistry getPlatformFrontendAdapterRegistry() {
        return DependencyLoaderAccess.findInstance(PlatformFrontendAdapterRegistry.class);
    }

    default PlatformFanoutTargetResolver getPlatformFanoutTargetResolver() {
        return DependencyLoaderAccess.findInstance(PlatformFanoutTargetResolver.class);
    }

    default PlatformFanoutResultAggregator getPlatformFanoutResultAggregator() {
        return DependencyLoaderAccess.findInstance(PlatformFanoutResultAggregator.class);
    }

    default ControlPlatformFanoutRetryHandler getControlPlatformFanoutRetryHandler() {
        return DependencyLoaderAccess.findInstance(ControlPlatformFanoutRetryHandler.class);
    }

    default Optional<ControlPlatformFanoutRetryHandler> getOptionalControlPlatformFanoutRetryHandler() {
        return DependencyLoaderAccess.findOptionalInstance(ControlPlatformFanoutRetryHandler.class);
    }

    default ControlCommandResultHandler getControlCommandResultHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandResultHandler.class);
    }

    default ControlCommandAuditLogHandler getControlCommandAuditLogHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandAuditLogHandler.class);
    }

    default ControlCommandResultRepository getControlCommandResultRepository() {
        return DependencyLoaderAccess.findInstance(ControlCommandResultRepository.class);
    }

    default ControlCommandAuditLogRepository getControlCommandAuditLogRepository() {
        return DependencyLoaderAccess.findInstance(ControlCommandAuditLogRepository.class);
    }

    default ScheduledControlCommandRepository getScheduledControlCommandRepository() {
        return DependencyLoaderAccess.findInstance(ScheduledControlCommandRepository.class);
    }

    default ControlPlatformFanoutRetryRepository getControlPlatformFanoutRetryRepository() {
        return DependencyLoaderAccess.findInstance(ControlPlatformFanoutRetryRepository.class);
    }

    default ControlCommandSerializer getControlCommandSerializer() {
        return DependencyLoaderAccess.findInstance(ControlCommandSerializer.class);
    }

    default ControlCommandSchedulingHandler getControlCommandSchedulingHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandSchedulingHandler.class);
    }

    default ControlCommandParsingHandler getControlCommandParsingHandler() {
        return DependencyLoaderAccess.findInstance(ControlCommandParsingHandler.class);
    }

    default KdControlCommandTranslationHandler getKdControlCommandTranslationHandler() {
        return DependencyLoaderAccess.findInstance(KdControlCommandTranslationHandler.class);
    }

    default ControlOperator getControlOperator() {
        return DependencyLoaderAccess.findInstance(ControlOperator.class);
    }

    default Clock getControlCommandClock() {
        return DependencyLoaderAccess.findInstance(Clock.class);
    }

    default Optional<IControlAuthorizationHandler> getOptionalControlAuthorizationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(IControlAuthorizationHandler.class);
    }

    default UniversalPlayerAccountRepository getUniversalPlayerAccountRepository() {
        return DependencyLoaderAccess.findInstance(UniversalPlayerAccountRepository.class);
    }

    default PlatformAccountBindingRepository getPlatformAccountBindingRepository() {
        return DependencyLoaderAccess.findInstance(PlatformAccountBindingRepository.class);
    }

    default GlobalAssetRepository getGlobalAssetRepository() {
        return DependencyLoaderAccess.findInstance(GlobalAssetRepository.class);
    }

    default PlatformAssetVersionRegistrationHandler getPlatformAssetVersionRegistrationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(PlatformAssetVersionRegistrationHandler.class)
                .orElseGet(() -> {
                    PlatformAssetVersionRegistrationHandler handler = new PlatformAssetVersionRegistrationHandler();
                    DependencyLoaderAccess.registerInstance(PlatformAssetVersionRegistrationHandler.class, handler);
                    return handler;
                });
    }

    default TroopRepository getTroopRepository() {
        return DependencyLoaderAccess.findInstance(TroopRepository.class);
    }

    default TroopHealingRepository getTroopHealingRepository() {
        return DependencyLoaderAccess.findInstance(TroopHealingRepository.class);
    }

    default HealingInventoryRepository getHealingInventoryRepository() {
        return DependencyLoaderAccess.findInstance(HealingInventoryRepository.class);
    }

    default HealingFacilityDefinitionRegistry getHealingFacilityDefinitionRegistry() {
        return DependencyLoaderAccess.findInstance(HealingFacilityDefinitionRegistry.class);
    }

    default TroopWoundAssignmentHandler getTroopWoundAssignmentHandler() {
        return DependencyLoaderAccess.findInstance(TroopWoundAssignmentHandler.class);
    }

    default TroopHealingRecipeSelectionHandler getTroopHealingRecipeSelectionHandler() {
        return DependencyLoaderAccess.findInstance(TroopHealingRecipeSelectionHandler.class);
    }

    default TroopHealingRecipeValidationHandler getTroopHealingRecipeValidationHandler() {
        return DependencyLoaderAccess.findInstance(TroopHealingRecipeValidationHandler.class);
    }

    default TroopHealingStartHandler getTroopHealingStartHandler() {
        return DependencyLoaderAccess.findInstance(TroopHealingStartHandler.class);
    }

    default TroopHealingProgressTickHandler getTroopHealingProgressTickHandler() {
        return DependencyLoaderAccess.findInstance(TroopHealingProgressTickHandler.class);
    }

    default ControlSurfaceLaunchHandler getControlSurfaceLaunchHandler() {
        return DependencyLoaderAccess.findInstance(ControlSurfaceLaunchHandler.class);
    }

    default UniversalKingdomSimulationSystem getUniversalKingdomSimulationSystem() {
        return DependencyLoaderAccess.findInstance(UniversalKingdomSimulationSystem.class);
    }

    default KingdomClockControlSystem getKingdomClockControlSystem() {
        return DependencyLoaderAccess.findInstance(KingdomClockControlSystem.class);
    }

    default CitizenControlSystem getCitizenControlSystem() {
        return DependencyLoaderAccess.findInstance(CitizenControlSystem.class);
    }

    default CompanionService getCompanionService() {
        return DependencyLoaderAccess.findInstance(CompanionService.class);
    }
}
