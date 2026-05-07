package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.asset.InMemoryGlobalAssetRepository;
import com.tavall.hytale.resourcegame.middleware.authority.AuthorityRepository;
import com.tavall.hytale.resourcegame.middleware.authority.AuthorityScope;
import com.tavall.hytale.resourcegame.middleware.authority.AuthorizationAuditRepository;
import com.tavall.hytale.resourcegame.middleware.authority.ControlAuthority;
import com.tavall.hytale.resourcegame.middleware.authority.ControlAuthorityLevel;
import com.tavall.hytale.resourcegame.middleware.authority.ControlAuthorizationHandler;
import com.tavall.hytale.resourcegame.middleware.authority.InMemoryAuthorityRepository;
import com.tavall.hytale.resourcegame.middleware.authority.InMemoryAuthorizationAuditRepository;
import com.tavall.hytale.resourcegame.middleware.authority.InMemoryPermissionPolicyRepository;
import com.tavall.hytale.resourcegame.middleware.authority.PermissionPolicyRepository;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenControlSystem;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockControlSystem;
import com.tavall.hytale.resourcegame.middleware.companion.CompanionService;
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
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import com.tavall.hytale.resourcegame.middleware.troop.InMemoryTroopRepository;
import com.tavall.hytale.resourcegame.persistence.PostgresConnectionProvider;

import java.time.Instant;
import java.time.Clock;
import java.util.EnumSet;
import java.util.List;

public final class ControlCommandRuntimeFactory {
    private ControlCommandRuntimeFactory() {
    }

    public static ControlCommandRuntime createInMemoryRuntime() {
        return createInMemoryRuntime(new RecordingControlSurfaceLaunchHandler());
    }

    public static ControlCommandRuntime createInMemoryRuntime(ControlSurfaceLaunchHandler surfaceLaunchHandler) {
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
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        InMemoryPermissionPolicyRepository permissionPolicyRepository = new InMemoryPermissionPolicyRepository();
        InMemoryAuthorizationAuditRepository authorizationAuditRepository = new InMemoryAuthorizationAuditRepository();
        ControlOperator localOwner = ControlOperator.localOwner(now);
        ControlOperator systemOperator = ControlOperator.system(now);
        operatorRepository.saveOperator(localOwner);
        operatorRepository.saveOperator(systemOperator);

        RecordingDomainEventPublisher eventPublisher = new RecordingDomainEventPublisher();
        UniversalKingdomSimulationSystem kingdomSimulationSystem = UniversalKingdomSimulationSystem.inMemory(eventPublisher);
        KingdomClockControlSystem kingdomClockSystem = KingdomClockControlSystem.inMemory(eventPublisher);
        CitizenControlSystem citizenControlSystem = CitizenControlSystem.inMemory(kingdomClockSystem);
        CompanionService companionService = CompanionService.inMemory();
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
                progressTickHandler,
                surfaceLaunchHandler,
                kingdomSimulationSystem,
                kingdomClockSystem,
                citizenControlSystem,
                companionService
        );
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler(fanoutRetryRepository);
        PlatformCommandFanoutHandler fanoutHandler = new PlatformCommandFanoutHandler(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.HYTALE, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ROBLOX, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.DISCORD, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ANDROID, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.PC, true)
        ), new PlatformFanoutTargetResolver(), fanoutRetryHandler);
        ControlCommandResultHandler resultHandler = new ControlCommandResultHandler(resultRepository);
        ControlCommandAuditLogHandler auditLogHandler = new ControlCommandAuditLogHandler(auditLogRepository, new ControlCommandSerializer());
        ControlCommandParsingHandler parsingHandler = new ControlCommandParsingHandler();
        seedDefaultAuthorities(authorityRepository, localOwner, systemOperator, now);
        ControlAuthorizationHandler authorizationHandler = new ControlAuthorizationHandler(
                authorityRepository,
                permissionPolicyRepository,
                authorizationAuditRepository,
                commandRegistry
        );
        ControlCommandDispatchHandler dispatchHandler = new ControlCommandDispatchHandler(
                commandRegistry,
                validationHandler,
                executionHandler,
                fanoutHandler,
                new PlatformFanoutResultAggregator(),
                resultHandler,
                auditLogHandler,
                authorizationHandler
        );
        FrontendCommandIngressHandler frontendCommandIngressHandler = new FrontendCommandIngressHandler(
                parsingHandler,
                dispatchHandler,
                new KdControlCommandTranslationHandler(),
                systemOperator
        );
        ControlCommandSchedulingHandler schedulingHandler = new ControlCommandSchedulingHandler(scheduledCommandRepository, dispatchHandler);
        ControlPlaneMaintenanceWorker maintenanceWorker = new ControlPlaneMaintenanceWorker(
                schedulingHandler,
                fanoutRetryHandler,
                kingdomClockSystem,
                kingdomSimulationSystem,
                Clock.systemUTC()
        );

        return new ControlCommandRuntime(
                dispatchHandler,
                frontendCommandIngressHandler,
                parsingHandler,
                commandRegistry,
                fanoutHandler,
                auditLogRepository,
                resultRepository,
                operatorRepository,
                fanoutRetryRepository,
                scheduledCommandRepository,
                schedulingHandler,
                maintenanceWorker,
                new ControlCommandCompensationHandler(),
                authorityRepository,
                permissionPolicyRepository,
                authorizationAuditRepository,
                authorizationHandler,
                identityRepository,
                identityRepository,
                assetRepository,
                kingdomSimulationSystem,
                kingdomClockSystem,
                citizenControlSystem,
                companionService,
                troopRepository,
                troopHealingRepository,
                healingInventoryRepository
        );
    }

    public static ControlCommandRuntime createPostgresRuntime(PostgresConnectionProvider connectionProvider) {
        return createPostgresRuntime(connectionProvider, new RecordingControlSurfaceLaunchHandler());
    }

    public static ControlCommandRuntime createPostgresRuntime(
            PostgresConnectionProvider connectionProvider,
            ControlSurfaceLaunchHandler surfaceLaunchHandler
    ) {
        Instant now = Instant.now();
        InMemoryIdentityRepository identityRepository = new InMemoryIdentityRepository();
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        InMemoryTroopRepository troopRepository = new InMemoryTroopRepository();
        TroopHealingRepository troopHealingRepository = new InMemoryTroopHealingRepository();
        HealingInventoryRepository healingInventoryRepository = new InMemoryHealingInventoryRepository();
        PostgresControlCommandAuditLogRepository auditLogRepository = new PostgresControlCommandAuditLogRepository(connectionProvider);
        PostgresControlCommandResultRepository resultRepository = new PostgresControlCommandResultRepository(connectionProvider);
        PostgresControlOperatorRepository operatorRepository = new PostgresControlOperatorRepository(connectionProvider);
        PostgresControlPlatformFanoutRetryRepository fanoutRetryRepository = new PostgresControlPlatformFanoutRetryRepository(connectionProvider);
        PostgresScheduledControlCommandRepository scheduledCommandRepository = new PostgresScheduledControlCommandRepository(connectionProvider);
        InMemoryAuthorityRepository authorityRepository = new InMemoryAuthorityRepository();
        InMemoryPermissionPolicyRepository permissionPolicyRepository = new InMemoryPermissionPolicyRepository();
        InMemoryAuthorizationAuditRepository authorizationAuditRepository = new InMemoryAuthorizationAuditRepository();
        ControlOperator localOwner = ControlOperator.localOwner(now);
        ControlOperator systemOperator = ControlOperator.system(now);
        operatorRepository.saveOperator(localOwner);
        operatorRepository.saveOperator(systemOperator);

        RecordingDomainEventPublisher eventPublisher = new RecordingDomainEventPublisher();
        UniversalKingdomSimulationSystem kingdomSimulationSystem = UniversalKingdomSimulationSystem.postgres(connectionProvider, eventPublisher);
        KingdomClockControlSystem kingdomClockSystem = KingdomClockControlSystem.postgres(connectionProvider, eventPublisher, Clock.systemUTC());
        CitizenControlSystem citizenControlSystem = CitizenControlSystem.postgres(connectionProvider, kingdomClockSystem);
        CompanionService companionService = CompanionService.postgres(connectionProvider);
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
                progressTickHandler,
                surfaceLaunchHandler,
                kingdomSimulationSystem,
                kingdomClockSystem,
                citizenControlSystem,
                companionService
        );
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler(fanoutRetryRepository);
        PlatformCommandFanoutHandler fanoutHandler = new PlatformCommandFanoutHandler(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.HYTALE, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ROBLOX, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.DISCORD, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ANDROID, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.PC, true)
        ), new PlatformFanoutTargetResolver(), fanoutRetryHandler);
        ControlCommandResultHandler resultHandler = new ControlCommandResultHandler(resultRepository);
        ControlCommandAuditLogHandler auditLogHandler = new ControlCommandAuditLogHandler(auditLogRepository, new ControlCommandSerializer());
        ControlCommandParsingHandler parsingHandler = new ControlCommandParsingHandler();
        seedDefaultAuthorities(authorityRepository, localOwner, systemOperator, now);
        ControlAuthorizationHandler authorizationHandler = new ControlAuthorizationHandler(
                authorityRepository,
                permissionPolicyRepository,
                authorizationAuditRepository,
                commandRegistry
        );
        ControlCommandDispatchHandler dispatchHandler = new ControlCommandDispatchHandler(
                commandRegistry,
                validationHandler,
                executionHandler,
                fanoutHandler,
                new PlatformFanoutResultAggregator(),
                resultHandler,
                auditLogHandler,
                authorizationHandler
        );
        FrontendCommandIngressHandler frontendCommandIngressHandler = new FrontendCommandIngressHandler(
                parsingHandler,
                dispatchHandler,
                new KdControlCommandTranslationHandler(),
                systemOperator
        );
        ControlCommandSchedulingHandler schedulingHandler = new ControlCommandSchedulingHandler(scheduledCommandRepository, dispatchHandler);
        ControlPlaneMaintenanceWorker maintenanceWorker = new ControlPlaneMaintenanceWorker(
                schedulingHandler,
                fanoutRetryHandler,
                kingdomClockSystem,
                kingdomSimulationSystem,
                Clock.systemUTC()
        );

        return new ControlCommandRuntime(
                dispatchHandler,
                frontendCommandIngressHandler,
                parsingHandler,
                commandRegistry,
                fanoutHandler,
                auditLogRepository,
                resultRepository,
                operatorRepository,
                fanoutRetryRepository,
                scheduledCommandRepository,
                schedulingHandler,
                maintenanceWorker,
                new ControlCommandCompensationHandler(),
                authorityRepository,
                permissionPolicyRepository,
                authorizationAuditRepository,
                authorizationHandler,
                identityRepository,
                identityRepository,
                assetRepository,
                kingdomSimulationSystem,
                kingdomClockSystem,
                citizenControlSystem,
                companionService,
                troopRepository,
                troopHealingRepository,
                healingInventoryRepository
        );
    }

    private static void seedDefaultAuthorities(
            AuthorityRepository authorityRepository,
            ControlOperator localOwner,
            ControlOperator systemOperator,
            Instant now
    ) {
        long grantedAt = now.toEpochMilli();
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                localOwner.operatorId(),
                ControlAuthorityLevel.C5_GLOBAL_AUTHORITY,
                AuthorityScope.global(),
                EnumSet.allOf(ControlPermission.class),
                localOwner.operatorId(),
                grantedAt,
                true,
                true
        ));
        authorityRepository.saveAuthority(ControlAuthority.enabled(
                systemOperator.operatorId(),
                ControlAuthorityLevel.C5_GLOBAL_AUTHORITY,
                AuthorityScope.global(),
                EnumSet.allOf(ControlPermission.class),
                localOwner.operatorId(),
                grantedAt,
                true,
                false
        ));
    }
}
