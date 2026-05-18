package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.asset.GlobalAssetRepository;
import org.tavall.control.asset.InMemoryGlobalAssetRepository;
import org.tavall.control.authority.AuthorityRepository;
import org.tavall.control.authority.AuthorityScope;
import org.tavall.control.authority.AuthorizationAuditRepository;
import org.tavall.control.authority.ControlAuthorityDependencyModule;
import org.tavall.control.authority.ControlAuthority;
import org.tavall.control.authority.ControlAuthorityLevel;
import org.tavall.control.authority.ControlAuthorityDomain;
import org.tavall.control.authority.InMemoryAuthorityRepository;
import org.tavall.control.authority.InMemoryAuthorizationAuditRepository;
import org.tavall.control.authority.InMemoryPermissionPolicyRepository;
import org.tavall.control.authority.PermissionPolicyRepository;
import org.tavall.control.citizen.CitizenControlSystem;
import org.tavall.control.clock.KingdomClockControlSystem;
import org.tavall.control.companion.CompanionHandler;
import org.tavall.control.common.GamePlatform;
import org.tavall.control.event.RecordingDomainEventPublisher;
import org.tavall.control.healing.HealingFacilityDefinitionRegistry;
import org.tavall.control.healing.HealingFacilityModifierCalculationHandler;
import org.tavall.control.healing.HealingInventoryRepository;
import org.tavall.control.healing.HealingResourceCostCalculationHandler;
import org.tavall.control.healing.InMemoryHealingInventoryRepository;
import org.tavall.control.healing.InMemoryTroopHealingRepository;
import org.tavall.control.healing.TroopHealingCompletionHandler;
import org.tavall.control.healing.TroopHealingProgressTickHandler;
import org.tavall.control.healing.TroopHealingRecipeSelectionHandler;
import org.tavall.control.healing.TroopHealingRecipeValidationHandler;
import org.tavall.control.healing.TroopHealingRepository;
import org.tavall.control.healing.TroopHealingStartHandler;
import org.tavall.control.healing.TroopWoundAssignmentHandler;
import org.tavall.control.identity.InMemoryIdentityRepository;
import org.tavall.control.identity.PlatformAccountBindingRepository;
import org.tavall.control.identity.UniversalPlayerAccountRepository;
import org.tavall.control.kingdom.UniversalKingdomSimulationSystem;
import org.tavall.control.troop.InMemoryTroopRepository;
import org.tavall.control.troop.TroopRepository;
import org.tavall.control.persistence.PostgresConnectionProvider;

import java.time.Instant;
import java.time.Clock;
import java.util.EnumSet;
import java.util.List;

public final class ControlCommandRuntimeFactory implements ControlAuthorityDomain {
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
        CompanionHandler companionHandler = CompanionHandler.inMemory();
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
                companionHandler
        );
        ControlCommandExecutionHandler executionHandler = new ControlCommandExecutionHandler();
        DependencyLoaderAccess.registerInstance(ControlPlatformFanoutRetryRepository.class, fanoutRetryRepository);
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler();
        PlatformFrontendAdapterRegistry platformAdapterRegistry = new PlatformFrontendAdapterRegistry();
        platformAdapterRegistry.registerAdapters(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
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
                companionHandler,
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
        CompanionHandler companionHandler = CompanionHandler.postgres(connectionProvider);
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
                companionHandler
        );
        ControlCommandExecutionHandler executionHandler = new ControlCommandExecutionHandler();
        DependencyLoaderAccess.registerInstance(ControlPlatformFanoutRetryRepository.class, fanoutRetryRepository);
        ControlPlatformFanoutRetryHandler fanoutRetryHandler = new ControlPlatformFanoutRetryHandler();
        PlatformFrontendAdapterRegistry platformAdapterRegistry = new PlatformFrontendAdapterRegistry();
        platformAdapterRegistry.registerAdapters(List.of(
                new InMemoryPlatformFrontendAdapter(GamePlatform.MINECRAFT, true),
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
                companionHandler,
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
            CompanionHandler companionHandler
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
        DependencyLoaderAccess.registerInstance(CompanionHandler.class, companionHandler);
    }
}
