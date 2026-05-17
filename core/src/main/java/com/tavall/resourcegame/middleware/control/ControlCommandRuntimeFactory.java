package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.asset.GlobalAssetRepository;
import com.tavall.resourcegame.middleware.asset.InMemoryGlobalAssetRepository;
import com.tavall.resourcegame.middleware.authority.AuthorityRepository;
import com.tavall.resourcegame.middleware.authority.AuthorityScope;
import com.tavall.resourcegame.middleware.authority.AuthorizationAuditRepository;
import com.tavall.resourcegame.middleware.authority.ControlAuthorityDependencyModule;
import com.tavall.resourcegame.middleware.authority.ControlAuthority;
import com.tavall.resourcegame.middleware.authority.ControlAuthorityLevel;
import com.tavall.resourcegame.middleware.authority.IControlAuthorityDomain;
import com.tavall.resourcegame.middleware.authority.InMemoryAuthorityRepository;
import com.tavall.resourcegame.middleware.authority.InMemoryAuthorizationAuditRepository;
import com.tavall.resourcegame.middleware.authority.InMemoryPermissionPolicyRepository;
import com.tavall.resourcegame.middleware.authority.PermissionPolicyRepository;
import com.tavall.resourcegame.middleware.citizen.CitizenControlSystem;
import com.tavall.resourcegame.middleware.clock.KingdomClockControlSystem;
import com.tavall.resourcegame.middleware.companion.CompanionService;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.event.RecordingDomainEventPublisher;
import com.tavall.resourcegame.middleware.healing.HealingFacilityDefinitionRegistry;
import com.tavall.resourcegame.middleware.healing.HealingFacilityModifierCalculationHandler;
import com.tavall.resourcegame.middleware.healing.HealingInventoryRepository;
import com.tavall.resourcegame.middleware.healing.HealingResourceCostCalculationHandler;
import com.tavall.resourcegame.middleware.healing.InMemoryHealingInventoryRepository;
import com.tavall.resourcegame.middleware.healing.InMemoryTroopHealingRepository;
import com.tavall.resourcegame.middleware.healing.TroopHealingCompletionHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingProgressTickHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingRecipeSelectionHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingRecipeValidationHandler;
import com.tavall.resourcegame.middleware.healing.TroopHealingRepository;
import com.tavall.resourcegame.middleware.healing.TroopHealingStartHandler;
import com.tavall.resourcegame.middleware.healing.TroopWoundAssignmentHandler;
import com.tavall.resourcegame.middleware.identity.InMemoryIdentityRepository;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBindingRepository;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerAccountRepository;
import com.tavall.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import com.tavall.resourcegame.middleware.troop.InMemoryTroopRepository;
import com.tavall.resourcegame.middleware.troop.TroopRepository;
import com.tavall.resourcegame.persistence.PostgresConnectionProvider;

import java.time.Instant;
import java.time.Clock;
import java.util.EnumSet;
import java.util.List;

public final class ControlCommandRuntimeFactory implements IControlAuthorityDomain {
    private static final ControlCommandRuntimeFactory INSTANCE = new ControlCommandRuntimeFactory();

    private ControlCommandRuntimeFactory() {
    }

    public static ControlCommandRuntime createInMemoryRuntime() {
        return INSTANCE.createInMemoryRuntimeInternal(new RecordingControlSurfaceLaunchHandler());
    }

    public static ControlCommandRuntime createInMemoryRuntime(ControlSurfaceLaunchHandler surfaceLaunchHandler) {
        return INSTANCE.createInMemoryRuntimeInternal(surfaceLaunchHandler);
    }

    private ControlCommandRuntime createInMemoryRuntimeInternal(ControlSurfaceLaunchHandler surfaceLaunchHandler) {
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
        DependencyLoaderAccess.registerInstance(ControlCommandRegistry.class, commandRegistry);
        ControlCommandValidationHandler validationHandler = new ControlCommandValidationHandler();
        registerCommandExecutionDependencies(
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
        ControlCommandExecutionHandler executionHandler = new ControlCommandExecutionHandler();
        DependencyLoaderAccess.registerInstance(ControlPlatformFanoutRetryRepository.class, fanoutRetryRepository);
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler();
        PlatformFrontendAdapterRegistry platformAdapterRegistry = new PlatformFrontendAdapterRegistry();
        platformAdapterRegistry.registerAdapters(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.HYTALE, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ROBLOX, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.DISCORD, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ANDROID, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.PC, true)
        ));
        DependencyLoaderAccess.registerInstance(PlatformFrontendAdapterRegistry.class, platformAdapterRegistry);
        DependencyLoaderAccess.registerInstance(PlatformFanoutTargetResolver.class, new PlatformFanoutTargetResolver());
        DependencyLoaderAccess.registerInstance(ControlPlatformFanoutRetryHandler.class, fanoutRetryHandler);
        PlatformCommandFanoutHandler fanoutHandler = new PlatformCommandFanoutHandler();
        DependencyLoaderAccess.registerInstance(ControlCommandResultRepository.class, resultRepository);
        DependencyLoaderAccess.registerInstance(ControlCommandAuditLogRepository.class, auditLogRepository);
        DependencyLoaderAccess.registerInstance(ControlCommandSerializer.class, new ControlCommandSerializer());
        ControlCommandResultHandler resultHandler = new ControlCommandResultHandler();
        ControlCommandAuditLogHandler auditLogHandler = new ControlCommandAuditLogHandler();
        ControlCommandParsingHandler parsingHandler = new ControlCommandParsingHandler();
        seedDefaultAuthorities(authorityRepository, localOwner, systemOperator, now);
        registerAuthorityDependencies(authorityRepository, permissionPolicyRepository, authorizationAuditRepository, commandRegistry);
        registerCommandRuntimeDependencies(
                commandRegistry,
                validationHandler,
                executionHandler,
                fanoutHandler,
                new PlatformFanoutResultAggregator(),
                resultHandler,
                auditLogHandler
        );
        ControlCommandDispatchHandler dispatchHandler = new ControlCommandDispatchHandler();
        DependencyLoaderAccess.registerInstance(ControlCommandDispatchHandler.class, dispatchHandler);
        DependencyLoaderAccess.registerInstance(ControlCommandParsingHandler.class, parsingHandler);
        DependencyLoaderAccess.registerInstance(KdControlCommandTranslationHandler.class, new KdControlCommandTranslationHandler());
        DependencyLoaderAccess.registerInstance(ControlOperator.class, systemOperator);
        FrontendCommandIngressHandler frontendCommandIngressHandler = new FrontendCommandIngressHandler();
        DependencyLoaderAccess.registerInstance(ScheduledControlCommandRepository.class, scheduledCommandRepository);
        ControlCommandSchedulingHandler schedulingHandler = new ControlCommandSchedulingHandler();
        DependencyLoaderAccess.registerInstance(ControlCommandSchedulingHandler.class, schedulingHandler);
        DependencyLoaderAccess.registerInstance(Clock.class, Clock.systemUTC());
        ControlPlaneMaintenanceWorker maintenanceWorker = new ControlPlaneMaintenanceWorker();

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
                getControlAuthorizationHandler(),
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
        return INSTANCE.createPostgresRuntimeInternal(connectionProvider, new RecordingControlSurfaceLaunchHandler());
    }

    public static ControlCommandRuntime createPostgresRuntime(
            PostgresConnectionProvider connectionProvider,
            ControlSurfaceLaunchHandler surfaceLaunchHandler
    ) {
        return INSTANCE.createPostgresRuntimeInternal(connectionProvider, surfaceLaunchHandler);
    }

    private ControlCommandRuntime createPostgresRuntimeInternal(
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
        DependencyLoaderAccess.registerInstance(ControlCommandRegistry.class, commandRegistry);
        ControlCommandValidationHandler validationHandler = new ControlCommandValidationHandler();
        registerCommandExecutionDependencies(
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
        ControlCommandExecutionHandler executionHandler = new ControlCommandExecutionHandler();
        DependencyLoaderAccess.registerInstance(ControlPlatformFanoutRetryRepository.class, fanoutRetryRepository);
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler();
        PlatformFrontendAdapterRegistry platformAdapterRegistry = new PlatformFrontendAdapterRegistry();
        platformAdapterRegistry.registerAdapters(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.HYTALE, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ROBLOX, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.DISCORD, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.ANDROID, true),
                new InMemoryPlatformFrontendAdapter(GamePlatform.PC, true)
        ));
        DependencyLoaderAccess.registerInstance(PlatformFrontendAdapterRegistry.class, platformAdapterRegistry);
        DependencyLoaderAccess.registerInstance(PlatformFanoutTargetResolver.class, new PlatformFanoutTargetResolver());
        DependencyLoaderAccess.registerInstance(ControlPlatformFanoutRetryHandler.class, fanoutRetryHandler);
        PlatformCommandFanoutHandler fanoutHandler = new PlatformCommandFanoutHandler();
        DependencyLoaderAccess.registerInstance(ControlCommandResultRepository.class, resultRepository);
        DependencyLoaderAccess.registerInstance(ControlCommandAuditLogRepository.class, auditLogRepository);
        DependencyLoaderAccess.registerInstance(ControlCommandSerializer.class, new ControlCommandSerializer());
        ControlCommandResultHandler resultHandler = new ControlCommandResultHandler();
        ControlCommandAuditLogHandler auditLogHandler = new ControlCommandAuditLogHandler();
        ControlCommandParsingHandler parsingHandler = new ControlCommandParsingHandler();
        seedDefaultAuthorities(authorityRepository, localOwner, systemOperator, now);
        registerAuthorityDependencies(authorityRepository, permissionPolicyRepository, authorizationAuditRepository, commandRegistry);
        registerCommandRuntimeDependencies(
                commandRegistry,
                validationHandler,
                executionHandler,
                fanoutHandler,
                new PlatformFanoutResultAggregator(),
                resultHandler,
                auditLogHandler
        );
        ControlCommandDispatchHandler dispatchHandler = new ControlCommandDispatchHandler();
        DependencyLoaderAccess.registerInstance(ControlCommandDispatchHandler.class, dispatchHandler);
        DependencyLoaderAccess.registerInstance(ControlCommandParsingHandler.class, parsingHandler);
        DependencyLoaderAccess.registerInstance(KdControlCommandTranslationHandler.class, new KdControlCommandTranslationHandler());
        DependencyLoaderAccess.registerInstance(ControlOperator.class, systemOperator);
        FrontendCommandIngressHandler frontendCommandIngressHandler = new FrontendCommandIngressHandler();
        DependencyLoaderAccess.registerInstance(ScheduledControlCommandRepository.class, scheduledCommandRepository);
        ControlCommandSchedulingHandler schedulingHandler = new ControlCommandSchedulingHandler();
        DependencyLoaderAccess.registerInstance(ControlCommandSchedulingHandler.class, schedulingHandler);
        DependencyLoaderAccess.registerInstance(Clock.class, Clock.systemUTC());
        ControlPlaneMaintenanceWorker maintenanceWorker = new ControlPlaneMaintenanceWorker();

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
                getControlAuthorizationHandler(),
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

    private static void registerAuthorityDependencies(
            AuthorityRepository authorityRepository,
            PermissionPolicyRepository permissionPolicyRepository,
            AuthorizationAuditRepository authorizationAuditRepository,
            ControlCommandRegistry commandRegistry
    ) {
        DependencyLoaderAccess.registerInstance(AuthorityRepository.class, authorityRepository);
        DependencyLoaderAccess.registerInstance(PermissionPolicyRepository.class, permissionPolicyRepository);
        DependencyLoaderAccess.registerInstance(AuthorizationAuditRepository.class, authorizationAuditRepository);
        DependencyLoaderAccess.registerInstance(ControlCommandRegistry.class, commandRegistry);
        new ControlAuthorityDependencyModule().registerDependencies();
    }

    private static void registerCommandRuntimeDependencies(
            ControlCommandRegistry commandRegistry,
            ControlCommandValidationHandler validationHandler,
            ControlCommandExecutionHandler executionHandler,
            PlatformCommandFanoutHandler fanoutHandler,
            PlatformFanoutResultAggregator fanoutResultAggregator,
            ControlCommandResultHandler resultHandler,
            ControlCommandAuditLogHandler auditLogHandler
    ) {
        DependencyLoaderAccess.registerInstance(ControlCommandRegistry.class, commandRegistry);
        DependencyLoaderAccess.registerInstance(ControlCommandValidationHandler.class, validationHandler);
        DependencyLoaderAccess.registerInstance(ControlCommandExecutionHandler.class, executionHandler);
        DependencyLoaderAccess.registerInstance(PlatformCommandFanoutHandler.class, fanoutHandler);
        DependencyLoaderAccess.registerInstance(PlatformFanoutResultAggregator.class, fanoutResultAggregator);
        DependencyLoaderAccess.registerInstance(ControlCommandResultHandler.class, resultHandler);
        DependencyLoaderAccess.registerInstance(ControlCommandAuditLogHandler.class, auditLogHandler);
    }

    private static void registerCommandExecutionDependencies(
            UniversalPlayerAccountRepository accountRepository,
            PlatformAccountBindingRepository platformAccountBindingRepository,
            GlobalAssetRepository globalAssetRepository,
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            HealingInventoryRepository healingInventoryRepository,
            HealingFacilityDefinitionRegistry healingFacilityDefinitionRegistry,
            TroopWoundAssignmentHandler woundAssignmentHandler,
            TroopHealingRecipeSelectionHandler recipeSelectionHandler,
            TroopHealingRecipeValidationHandler recipeValidationHandler,
            TroopHealingStartHandler healingStartHandler,
            TroopHealingProgressTickHandler healingProgressTickHandler,
            ControlSurfaceLaunchHandler surfaceLaunchHandler,
            UniversalKingdomSimulationSystem kingdomSimulationSystem,
            KingdomClockControlSystem kingdomClockSystem,
            CitizenControlSystem citizenControlSystem,
            CompanionService companionService
    ) {
        DependencyLoaderAccess.registerInstance(UniversalPlayerAccountRepository.class, accountRepository);
        DependencyLoaderAccess.registerInstance(PlatformAccountBindingRepository.class, platformAccountBindingRepository);
        DependencyLoaderAccess.registerInstance(GlobalAssetRepository.class, globalAssetRepository);
        DependencyLoaderAccess.registerInstance(TroopRepository.class, troopRepository);
        DependencyLoaderAccess.registerInstance(TroopHealingRepository.class, troopHealingRepository);
        DependencyLoaderAccess.registerInstance(HealingInventoryRepository.class, healingInventoryRepository);
        DependencyLoaderAccess.registerInstance(HealingFacilityDefinitionRegistry.class, healingFacilityDefinitionRegistry);
        DependencyLoaderAccess.registerInstance(TroopWoundAssignmentHandler.class, woundAssignmentHandler);
        DependencyLoaderAccess.registerInstance(TroopHealingRecipeSelectionHandler.class, recipeSelectionHandler);
        DependencyLoaderAccess.registerInstance(TroopHealingRecipeValidationHandler.class, recipeValidationHandler);
        DependencyLoaderAccess.registerInstance(TroopHealingStartHandler.class, healingStartHandler);
        DependencyLoaderAccess.registerInstance(TroopHealingProgressTickHandler.class, healingProgressTickHandler);
        DependencyLoaderAccess.registerInstance(ControlSurfaceLaunchHandler.class, surfaceLaunchHandler);
        DependencyLoaderAccess.registerInstance(UniversalKingdomSimulationSystem.class, kingdomSimulationSystem);
        DependencyLoaderAccess.registerInstance(KingdomClockControlSystem.class, kingdomClockSystem);
        DependencyLoaderAccess.registerInstance(CitizenControlSystem.class, citizenControlSystem);
        DependencyLoaderAccess.registerInstance(CompanionService.class, companionService);
    }
}
