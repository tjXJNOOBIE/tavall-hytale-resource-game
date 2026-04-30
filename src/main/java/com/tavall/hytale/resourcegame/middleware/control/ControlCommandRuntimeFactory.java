package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.asset.InMemoryGlobalAssetRepository;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.event.RecordingDomainEventPublisher;
import com.tavall.hytale.resourcegame.middleware.healing.HealingFacilityDefinitionRegistry;
import com.tavall.hytale.resourcegame.middleware.healing.HealingFacilityModifierCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.healing.HealingInventoryRepository;
import com.tavall.hytale.resourcegame.middleware.healing.HealingResourceCostCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.healing.InMemoryHealingInventoryRepository;
import com.tavall.hytale.resourcegame.middleware.healing.InMemoryTroopHealingRepository;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingCompletionHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingProgressTickHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRecipeSelectionHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRecipeValidationHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingRepository;
import com.tavall.hytale.resourcegame.middleware.healing.TroopHealingStartHandler;
import com.tavall.hytale.resourcegame.middleware.healing.TroopWoundAssignmentHandler;
import com.tavall.hytale.resourcegame.middleware.identity.InMemoryIdentityRepository;
import com.tavall.hytale.resourcegame.middleware.troop.InMemoryTroopRepository;

import java.time.Instant;
import java.util.List;

public final class ControlCommandRuntimeFactory {
    private ControlCommandRuntimeFactory() {
    }

    public static ControlCommandRuntime createInMemoryRuntime() {
        Instant now = Instant.now();
        InMemoryIdentityRepository identityRepository = new InMemoryIdentityRepository();
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        InMemoryTroopRepository troopRepository = new InMemoryTroopRepository();
        TroopHealingRepository troopHealingRepository = new InMemoryTroopHealingRepository();
        HealingInventoryRepository healingInventoryRepository = new InMemoryHealingInventoryRepository();
        InMemoryControlCommandAuditLogRepository auditLogRepository = new InMemoryControlCommandAuditLogRepository();
        InMemoryControlCommandResultRepository resultRepository = new InMemoryControlCommandResultRepository();
        InMemoryControlOperatorRepository operatorRepository = new InMemoryControlOperatorRepository();
        InMemoryControlPlatformFanoutRetryRepository fanoutRetryRepository = new InMemoryControlPlatformFanoutRetryRepository();
        InMemoryScheduledControlCommandRepository scheduledCommandRepository = new InMemoryScheduledControlCommandRepository();
        operatorRepository.saveOperator(ControlOperator.localOwner(now));
        operatorRepository.saveOperator(ControlOperator.system(now));

        RecordingDomainEventPublisher eventPublisher = new RecordingDomainEventPublisher();
        HealingFacilityDefinitionRegistry facilityDefinitionRegistry = new HealingFacilityDefinitionRegistry();
        HealingFacilityModifierCalculationHandler modifierCalculationHandler = new HealingFacilityModifierCalculationHandler();
        HealingResourceCostCalculationHandler costCalculationHandler = new HealingResourceCostCalculationHandler(modifierCalculationHandler);
        TroopHealingRecipeSelectionHandler recipeSelectionHandler = new TroopHealingRecipeSelectionHandler();
        TroopHealingRecipeValidationHandler recipeValidationHandler = new TroopHealingRecipeValidationHandler(costCalculationHandler, modifierCalculationHandler);
        TroopWoundAssignmentHandler woundAssignmentHandler = new TroopWoundAssignmentHandler(troopRepository, troopHealingRepository, eventPublisher);
        TroopHealingCompletionHandler completionHandler = new TroopHealingCompletionHandler(troopRepository, troopHealingRepository, eventPublisher);
        TroopHealingStartHandler healingStartHandler = new TroopHealingStartHandler(troopRepository, troopHealingRepository, healingInventoryRepository, recipeValidationHandler, eventPublisher);
        TroopHealingProgressTickHandler progressTickHandler = new TroopHealingProgressTickHandler(troopHealingRepository, completionHandler, eventPublisher);

        ControlCommandRegistry commandRegistry = new ControlCommandRegistry();
        ControlCommandValidationHandler validationHandler = new ControlCommandValidationHandler(commandRegistry);
        ControlCommandExecutionHandler executionHandler = new ControlCommandExecutionHandler(
                identityRepository,
                identityRepository,
                assetRepository,
                troopRepository,
                troopHealingRepository,
                healingInventoryRepository,
                facilityDefinitionRegistry,
                woundAssignmentHandler,
                recipeSelectionHandler,
                recipeValidationHandler,
                healingStartHandler,
                progressTickHandler
        );
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler(fanoutRetryRepository);
        PlatformCommandFanoutHandler fanoutHandler = new PlatformCommandFanoutHandler(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.HYTALE, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ROBLOX, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.DISCORD, true)
        ), new PlatformFanoutTargetResolver(), fanoutRetryHandler);
        ControlCommandResultHandler resultHandler = new ControlCommandResultHandler(resultRepository);
        ControlCommandAuditLogHandler auditLogHandler = new ControlCommandAuditLogHandler(auditLogRepository, new ControlCommandSerializer());
        ControlCommandDispatchHandler dispatchHandler = new ControlCommandDispatchHandler(
                commandRegistry,
                validationHandler,
                executionHandler,
                fanoutHandler,
                new PlatformFanoutResultAggregator(),
                resultHandler,
                auditLogHandler
        );

        return new ControlCommandRuntime(
                dispatchHandler,
                new ControlCommandParsingHandler(),
                commandRegistry,
                fanoutHandler,
                auditLogRepository,
                resultRepository,
                operatorRepository,
                fanoutRetryRepository,
                scheduledCommandRepository,
                new ControlCommandSchedulingHandler(scheduledCommandRepository, dispatchHandler),
                new ControlCommandCompensationHandler(),
                identityRepository,
                identityRepository,
                assetRepository,
                troopRepository,
                troopHealingRepository,
                healingInventoryRepository
        );
    }
}
