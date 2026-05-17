package com.tavall.resourcegame.dependency.modules;

import com.tavall.resourcegame.ResourceGamePlugin;
import com.tavall.resourcegame.cache.JacksonCacheCodec;
import com.tavall.resourcegame.cache.SemanticCacheFactory;
import com.tavall.resourcegame.clock.KingdomClockService;
import com.tavall.resourcegame.config.CacheConfig;
import com.tavall.resourcegame.config.CastleAssetConfig;
import com.tavall.resourcegame.config.DatabaseConfig;
import com.tavall.resourcegame.config.KingdomClockConfig;
import com.tavall.resourcegame.config.PopulationDisplayConfig;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.dependency.IDependencyModule;
import com.tavall.resourcegame.dependency.interfaces.IBuildingInteractionService;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingService;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingVisualService;
import com.tavall.resourcegame.dependency.interfaces.ICastleInteractionService;
import com.tavall.resourcegame.dependency.interfaces.ICastleEconomySimulationService;
import com.tavall.resourcegame.dependency.interfaces.ICastlePromptLaneService;
import com.tavall.resourcegame.dependency.interfaces.ICastleProximityPromptService;
import com.tavall.resourcegame.dependency.interfaces.ICastlePlacementService;
import com.tavall.resourcegame.dependency.interfaces.ICastleSiteVisualService;
import com.tavall.resourcegame.dependency.interfaces.ICastleSpawnService;
import com.tavall.resourcegame.dependency.interfaces.ICustomEntitySpawnService;
import com.tavall.resourcegame.dependency.interfaces.IDebugCommandService;
import com.tavall.resourcegame.dependency.interfaces.IFocusedWorldInteractionService;
import com.tavall.resourcegame.dependency.interfaces.IFocusedWorldOverrideService;
import com.tavall.resourcegame.dependency.interfaces.IFarmsteadMenuService;
import com.tavall.resourcegame.dependency.interfaces.IFrontendCommandVerificationService;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlCommandClient;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlConfig;
import com.tavall.resourcegame.dependency.interfaces.IInfrastructureHealthService;
import com.tavall.resourcegame.dependency.interfaces.IInteriorInstanceService;
import com.tavall.resourcegame.dependency.interfaces.IInteriorWorldService;
import com.tavall.resourcegame.dependency.interfaces.IIpHashService;
import com.tavall.resourcegame.dependency.interfaces.IKingdomClockService;
import com.tavall.resourcegame.dependency.interfaces.IPlacementInteractionService;
import com.tavall.resourcegame.dependency.interfaces.IPlacementModeService;
import com.tavall.resourcegame.dependency.interfaces.IPlacementPreviewService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerDataService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerGameStateService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerProfileService;
import com.tavall.resourcegame.dependency.interfaces.IPlayerSessionStore;
import com.tavall.resourcegame.dependency.interfaces.IPlayerTeleportService;
import com.tavall.resourcegame.dependency.interfaces.IPopulationService;
import com.tavall.resourcegame.dependency.interfaces.IProtectedBlockSystemService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeInteractionService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodePromptLaneService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeVisualPulseService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeVisualService;
import com.tavall.resourcegame.dependency.interfaces.IResourceService;
import com.tavall.resourcegame.dependency.interfaces.IUiActionService;
import com.tavall.resourcegame.dependency.interfaces.IUiNavigator;
import com.tavall.resourcegame.dependency.interfaces.IUiPageRegistry;
import com.tavall.resourcegame.dependency.interfaces.IVisualVerificationControlHandler;
import com.tavall.resourcegame.dependency.interfaces.IWorkerNpcInteractionService;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.PlayerProfile;
import com.tavall.resourcegame.farmstead.npc.FarmsteadStewardSpawner;
import com.tavall.resourcegame.farmstead.ui.FarmsteadMenuService;
import com.tavall.resourcegame.interior.InteriorLayoutService;
import com.tavall.resourcegame.interior.InteriorStructureService;
import com.tavall.resourcegame.persistence.InMemoryPlayerGameStateStore;
import com.tavall.resourcegame.persistence.InMemoryPlayerProfileStore;
import com.tavall.resourcegame.persistence.PersistenceStoreBootstrap;
import com.tavall.resourcegame.persistence.PlayerGameStateRepository;
import com.tavall.resourcegame.persistence.PlayerGameStateStore;
import com.tavall.resourcegame.persistence.PlayerProfileRepository;
import com.tavall.resourcegame.persistence.PlayerProfileStore;
import com.tavall.resourcegame.persistence.PostgresConnectionProvider;
import com.tavall.resourcegame.persistence.ResolvedPersistenceStores;
import com.tavall.resourcegame.population.PromotionCost;
import com.tavall.resourcegame.services.CastleInteractionService;
import com.tavall.resourcegame.services.BuildingInteractionService;
import com.tavall.resourcegame.services.BuildingPlacementPlanner;
import com.tavall.resourcegame.services.CastleBuildingService;
import com.tavall.resourcegame.services.CastleBuildingVisualService;
import com.tavall.resourcegame.services.CastleEconomyPlanner;
import com.tavall.resourcegame.services.CastleEconomySimulationService;
import com.tavall.resourcegame.services.CastlePromptLaneService;
import com.tavall.resourcegame.services.CastleProximityPromptService;
import com.tavall.resourcegame.services.CastlePlacementService;
import com.tavall.resourcegame.services.CastleSiteScenePlanner;
import com.tavall.resourcegame.services.CastleSiteVisualService;
import com.tavall.resourcegame.services.CastleSpawnService;
import com.tavall.resourcegame.services.CustomEntitySpawnService;
import com.tavall.resourcegame.services.DebugCommandService;
import com.tavall.resourcegame.services.FocusedWorldInteractionService;
import com.tavall.resourcegame.services.FocusedWorldOverrideService;
import com.tavall.resourcegame.services.FocusedWorldTargetPlanner;
import com.tavall.resourcegame.services.FrontendCommandVerificationHandler;
import com.tavall.resourcegame.services.FrontendControlConfig;
import com.tavall.resourcegame.services.FrontendTcpControlCommandClient;
import com.tavall.resourcegame.services.InteriorInstanceService;
import com.tavall.resourcegame.services.InteriorTourMarkerService;
import com.tavall.resourcegame.services.InteriorWorldService;
import com.tavall.resourcegame.services.IpHashService;
import com.tavall.resourcegame.services.InfrastructureHealthService;
import com.tavall.resourcegame.services.JsonMapperProvider;
import com.tavall.resourcegame.services.NpcRoleResolver;
import com.tavall.resourcegame.services.NpcVisualSpawner;
import com.tavall.resourcegame.services.PlacementInteractionService;
import com.tavall.resourcegame.services.PlacementModeService;
import com.tavall.resourcegame.services.PlacementPreviewService;
import com.tavall.resourcegame.services.PlayerDataService;
import com.tavall.resourcegame.services.PlayerGameStateService;
import com.tavall.resourcegame.services.PlayerProfileService;
import com.tavall.resourcegame.services.PlayerSessionStore;
import com.tavall.resourcegame.services.PlayerTeleportService;
import com.tavall.resourcegame.services.PopulationDisplayGateway;
import com.tavall.resourcegame.services.PopulationDisplayService;
import com.tavall.resourcegame.services.ProtectedBlockSystemService;
import com.tavall.resourcegame.services.PopulationService;
import com.tavall.resourcegame.services.ResourceNodeInteractionService;
import com.tavall.resourcegame.services.ResourceNodePromptLaneService;
import com.tavall.resourcegame.services.ResourceNodeRoutePlanner;
import com.tavall.resourcegame.services.ResourceNodeService;
import com.tavall.resourcegame.services.ResourceNodeVisualPulseService;
import com.tavall.resourcegame.services.ResourceNodeVisualService;
import com.tavall.resourcegame.services.ResourceService;
import com.tavall.resourcegame.services.StructureProtectionService;
import com.tavall.resourcegame.services.VisualVerificationControlService;
import com.tavall.resourcegame.services.WorkerNpcInteractionService;
import com.tavall.resourcegame.services.WorldLabelService;
import com.tavall.resourcegame.ui.CastleCitizensPage;
import com.tavall.resourcegame.ui.CastleBuildingsPage;
import com.tavall.resourcegame.ui.CastleInfoPage;
import com.tavall.resourcegame.ui.CastleMainPage;
import com.tavall.resourcegame.ui.CastleResourcesPage;
import com.tavall.resourcegame.ui.CastleTroopsPage;
import com.tavall.resourcegame.ui.CastleUpgradesPage;
import com.tavall.resourcegame.ui.BuildingDetailPage;
import com.tavall.resourcegame.ui.DebugCommandPage;
import com.tavall.resourcegame.ui.DebugNavigatorPage;
import com.tavall.resourcegame.ui.DebugUiCommandBindings;
import com.tavall.resourcegame.ui.FarmsteadMenuPage;
import com.tavall.resourcegame.ui.InteriorMainPage;
import com.tavall.resourcegame.ui.ResourceNodePage;
import com.tavall.resourcegame.ui.UiActionService;
import com.tavall.resourcegame.ui.UiNavigator;
import com.tavall.resourcegame.ui.UiPageRegistry;
import com.tavall.resourcegame.ui.UiPageType;
import com.tavall.resourcegame.world.CastleEntityRegistry;
import com.tavall.resourcegame.world.CastleBuildingStructureService;
import com.tavall.resourcegame.world.BuildingPlacementStageStructureService;
import com.tavall.resourcegame.world.CastleSiteLayoutService;
import com.tavall.resourcegame.world.CastleSiteStructureService;
import com.tavall.resourcegame.world.CastlePromptLaneLayoutService;
import com.tavall.resourcegame.world.CastlePromptLaneStructureService;
import com.tavall.resourcegame.world.ResourceNodePromptLaneLayoutService;
import com.tavall.resourcegame.world.ResourceNodePromptLaneStructureService;
import com.tavall.resourcegame.world.ResourceNodeStructureService;
import com.tavall.resourcegame.commands.KingdomInteractionCommandSupport;
import com.tavall.resourcegame.commands.KingdomBuildingCommandSupport;
import com.tavall.resourcegame.commands.KingdomNodeCommandSupport;
import com.tavall.resourcegame.commands.KingdomPlacementCommandSupport;
import com.tavall.resourcegame.commands.KingdomHologramCommandSupport;
import com.tavall.resourcegame.commands.KingdomEntityCommandSupport;
import org.tavall.abstractcache.semantic.SemanticCache;

import java.util.logging.Logger;

/**
 * Repo-local composition root that mirrors the shared Tavall DI bootstrap style.
 */
public final class ResourceGameDependencyModule implements IDependencyModule {
    private final ResourceGamePlugin plugin;

    public ResourceGameDependencyModule(ResourceGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerDependencies() {
        CacheConfig cacheConfig = CacheConfig.fromEnv();
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnv();
        KingdomClockConfig clockConfig = KingdomClockConfig.fromEnv();

        JsonMapperProvider mapperProvider = new JsonMapperProvider();
        SemanticCacheFactory cacheFactory = new SemanticCacheFactory(cacheConfig);
        SemanticCache profileCache = cacheFactory.build("resource-game-profile");
        SemanticCache gameStateCache = cacheFactory.build("resource-game-game-state");

        PersistenceStoreBootstrap persistenceBootstrap = new PersistenceStoreBootstrap(Logger.getLogger(ResourceGameDependencyModule.class.getName()));
        ResolvedPersistenceStores persistenceStores = persistenceBootstrap.resolve(databaseConfig);
        PlayerProfileStore profileStore = persistenceStores.profileStore();
        PlayerGameStateStore gameStateStore = persistenceStores.gameStateStore();

        PlayerSessionStore sessionStore = new PlayerSessionStore();
        PlayerProfileService profileService = new PlayerProfileService(
                profileStore,
                profileCache,
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerProfile.class, "player-profile")
        );
        PlayerGameStateService gameStateService = new PlayerGameStateService(
                gameStateStore,
                gameStateCache,
                new JacksonCacheCodec<>(mapperProvider.mapper(), PlayerGameState.class, "player-game-state"),
                mapperProvider.mapper()
        );

        CastleAssetConfig castleAssetConfig = CastleAssetConfig.defaults();
        PopulationDisplayConfig populationDisplayConfig = PopulationDisplayConfig.defaults();
        InteriorLayoutService interiorLayoutService = new InteriorLayoutService();
        InteriorStructureService interiorStructureService = new InteriorStructureService();
        InfrastructureHealthService infrastructureHealthService = new InfrastructureHealthService(cacheConfig, databaseConfig);
        CastleEconomyPlanner economyPlanner = new CastleEconomyPlanner();
        WorldLabelService worldLabelService = new WorldLabelService();
        NpcVisualSpawner npcVisualSpawner = new NpcVisualSpawner();
        NpcRoleResolver npcRoleResolver = new NpcRoleResolver();
        FarmsteadStewardSpawner farmsteadStewardSpawner = new FarmsteadStewardSpawner(npcVisualSpawner, npcRoleResolver);
        StructureProtectionService structureProtectionService = new StructureProtectionService();
        ProtectedBlockSystemService protectedBlockSystemService = new ProtectedBlockSystemService(structureProtectionService);
        InteriorInstanceService interiorInstanceService = new InteriorInstanceService();
        CastleBuildingService buildingService = new CastleBuildingService();
        CastleBuildingVisualService buildingVisualService = new CastleBuildingVisualService(
                buildingService,
                new CastleBuildingStructureService(),
                worldLabelService,
                structureProtectionService
        );
        BuildingPlacementStageStructureService buildingPlacementStageStructureService = new BuildingPlacementStageStructureService();
        BuildingPlacementPlanner buildingPlacementPlanner = new BuildingPlacementPlanner(
                buildingService,
                interiorInstanceService,
                gameStateService,
                interiorLayoutService
        );
        ResourceNodeService resourceNodeService = new ResourceNodeService(sessionStore, gameStateService, mapperProvider.mapper(), economyPlanner);
        ResourceNodeRoutePlanner resourceNodeRoutePlanner = new ResourceNodeRoutePlanner();
        ResourceNodeVisualService resourceNodeVisualService = new ResourceNodeVisualService(
                resourceNodeService,
                new ResourceNodeStructureService(),
                worldLabelService,
                structureProtectionService
        );
        ResourceNodeVisualPulseService resourceNodeVisualPulseService = new ResourceNodeVisualPulseService(sessionStore, resourceNodeVisualService);
        CastleSiteVisualService castleSiteVisualService = new CastleSiteVisualService(
                castleAssetConfig,
                new CastleSiteLayoutService(),
                new CastleSiteStructureService(castleAssetConfig),
                worldLabelService,
                structureProtectionService,
                sessionStore
        );

        CastleSpawnService castleSpawnService = new CastleSpawnService(castleAssetConfig, sessionStore, castleSiteVisualService);
        PopulationDisplayService populationDisplayService = new PopulationDisplayService(populationDisplayConfig, worldLabelService);
        InteriorTourMarkerService interiorTourMarkerService = new InteriorTourMarkerService(worldLabelService);
        PlayerTeleportService playerTeleportService = new PlayerTeleportService();
        IpHashService ipHashService = new IpHashService();
        PlacementPreviewService placementPreviewService = new PlacementPreviewService(worldLabelService);
        CastlePromptLaneService castlePromptLaneService = new CastlePromptLaneService(
                new CastlePromptLaneLayoutService(),
                new CastlePromptLaneStructureService(),
                playerTeleportService
        );
        ResourceNodePromptLaneService resourceNodePromptLaneService = new ResourceNodePromptLaneService(
                new ResourceNodePromptLaneLayoutService(),
                new ResourceNodePromptLaneStructureService(),
                playerTeleportService
        );
        UiPageRegistry pageRegistry = new UiPageRegistry();
        UiNavigator uiNavigator = new UiNavigator(pageRegistry);
        WorkerNpcInteractionService workerNpcInteractionService = new WorkerNpcInteractionService(populationDisplayService, sessionStore, uiNavigator);
        ResourceService resourceService = new ResourceService(sessionStore, gameStateService, castleSiteVisualService, uiNavigator);
        PopulationService populationService = new PopulationService(
                sessionStore,
                gameStateService,
                resourceService,
                castleSiteVisualService,
                populationDisplayService,
                PromotionCost.defaultCost(),
                buildingService,
                resourceNodeService,
                resourceNodeVisualService,
                uiNavigator
        );
        CastleEconomySimulationService castleEconomySimulationService = new CastleEconomySimulationService(
                sessionStore,
                gameStateService,
                buildingService,
                buildingVisualService,
                castleSiteVisualService,
                economyPlanner,
                resourceNodeService,
                resourceNodeVisualService,
                uiNavigator
        );
        CastlePlacementService castlePlacementService = new CastlePlacementService(
                sessionStore,
                gameStateService,
                castleSpawnService,
                castleSiteVisualService,
                buildingVisualService,
                resourceNodeVisualService
        );
        PlacementModeService placementModeService = new PlacementModeService(
                sessionStore,
                placementPreviewService,
                buildingService,
                buildingVisualService,
                castlePlacementService,
                resourceNodeService,
                resourceNodeVisualService
        );
        PlacementInteractionService placementInteractionService = new PlacementInteractionService(placementModeService);
        InteriorWorldService interiorWorldService = new InteriorWorldService(
                sessionStore,
                gameStateService,
                interiorInstanceService,
                interiorLayoutService,
                interiorStructureService,
                interiorTourMarkerService,
                playerTeleportService,
                populationDisplayService,
                buildingVisualService,
                uiNavigator
        );
        FarmsteadMenuService farmsteadMenuService = new FarmsteadMenuService(sessionStore, buildingService, uiNavigator);

        KingdomClockService clockService = new KingdomClockService(clockConfig);
        PlayerDataService playerDataService = new PlayerDataService(
                profileService,
                gameStateService,
                sessionStore,
                castleSpawnService,
                interiorInstanceService,
                ipHashService,
                clockService,
                resourceNodeVisualService,
                buildingVisualService,
                populationDisplayService,
                interiorTourMarkerService,
                uiNavigator
        );
        VisualVerificationControlService visualVerificationControlService = new VisualVerificationControlService(
                playerDataService,
                sessionStore,
                uiNavigator
        );
        CastleInteractionService castleInteractionService = new CastleInteractionService(
                sessionStore,
                uiNavigator,
                castleAssetConfig
        );
        FocusedWorldOverrideService focusedWorldOverrideService = new FocusedWorldOverrideService();
        FocusedWorldTargetPlanner focusedWorldTargetPlanner = new FocusedWorldTargetPlanner();
        FocusedWorldInteractionService focusedWorldInteractionService = new FocusedWorldInteractionService(
                sessionStore,
                buildingService,
                castleInteractionService,
                focusedWorldOverrideService,
                resourceNodeService,
                uiNavigator,
                focusedWorldTargetPlanner
        );
        CastleProximityPromptService castleProximityPromptService = new CastleProximityPromptService(castleInteractionService, placementModeService);
        ResourceNodeInteractionService resourceNodeInteractionService = new ResourceNodeInteractionService(
                sessionStore,
                resourceNodeVisualService,
                focusedWorldInteractionService,
                uiNavigator
        );
        BuildingInteractionService buildingInteractionService = new BuildingInteractionService(sessionStore, buildingVisualService, focusedWorldInteractionService, uiNavigator);
        CustomEntitySpawnService customEntitySpawnService = new CustomEntitySpawnService(
                npcVisualSpawner,
                farmsteadStewardSpawner,
                npcRoleResolver,
                sessionStore,
                buildingService,
                uiNavigator,
                farmsteadMenuService
        );
        UiActionService uiActionService = new UiActionService();
        registerUiPages(pageRegistry, uiActionService, infrastructureHealthService, gameStateService, economyPlanner, resourceNodeService, buildingService);
        KingdomPlacementCommandSupport placementCommandSupport = new KingdomPlacementCommandSupport();
        KingdomBuildingCommandSupport buildingCommandSupport = new KingdomBuildingCommandSupport(
                buildingService,
                buildingVisualService,
                uiNavigator,
                playerTeleportService,
                placementModeService,
                focusedWorldInteractionService,
                buildingPlacementPlanner,
                buildingPlacementStageStructureService
        );
        KingdomNodeCommandSupport nodeCommandSupport = new KingdomNodeCommandSupport(
                resourceNodeService,
                resourceNodeVisualService,
                uiNavigator,
                playerTeleportService,
                placementModeService,
                resourceNodePromptLaneService,
                focusedWorldInteractionService,
                focusedWorldOverrideService
        );
        KingdomInteractionCommandSupport interactionCommandSupport = new KingdomInteractionCommandSupport();
        KingdomHologramCommandSupport hologramCommandSupport = new KingdomHologramCommandSupport();
        KingdomEntityCommandSupport entityCommandSupport = new KingdomEntityCommandSupport();
        FrontendControlConfig frontendControlConfig = FrontendControlConfig.fromEnvironment(System.getenv());
        FrontendCommandVerificationHandler frontendCommandVerificationService = new FrontendCommandVerificationHandler();
        DebugCommandService debugCommandService = new DebugCommandService(
                sessionStore,
                uiNavigator,
                populationService,
                resourceService,
                interiorWorldService,
                castleSpawnService,
                castlePromptLaneService,
                focusedWorldOverrideService,
                playerDataService,
                gameStateService,
                infrastructureHealthService,
                buildingService,
                buildingVisualService,
                resourceNodeService,
                resourceNodeVisualService,
                castleSiteVisualService,
                castleEconomySimulationService,
                playerTeleportService,
                placementModeService,
                buildingCommandSupport,
                nodeCommandSupport,
                placementCommandSupport,
                interactionCommandSupport,
                hologramCommandSupport,
                entityCommandSupport,
                frontendCommandVerificationService
        );

        registerSingleton(IPlayerProfileService.class, profileService);
        registerSingleton(com.fasterxml.jackson.databind.ObjectMapper.class, mapperProvider.mapper());
        registerSingleton(IFrontendControlConfig.class, frontendControlConfig);
        registerSingleton(IFrontendControlCommandClient.class, new FrontendTcpControlCommandClient());
        registerSingleton(IPlayerGameStateService.class, gameStateService);
        registerSingleton(IPlayerSessionStore.class, sessionStore);
        registerSingleton(ICastleBuildingService.class, buildingService);
        registerSingleton(ICastleBuildingVisualService.class, buildingVisualService);
        registerSingleton(InteriorLayoutService.class, interiorLayoutService);
        registerSingleton(CastleEconomyPlanner.class, economyPlanner);
        registerSingleton(BuildingPlacementStageStructureService.class, buildingPlacementStageStructureService);
        registerSingleton(BuildingPlacementPlanner.class, buildingPlacementPlanner);
        registerSingleton(ICastleEconomySimulationService.class, castleEconomySimulationService);
        registerSingleton(ICastleSiteVisualService.class, castleSiteVisualService);
        registerSingleton(IResourceNodeService.class, resourceNodeService);
        registerSingleton(IResourceNodeVisualService.class, resourceNodeVisualService);
        registerSingleton(IResourceNodeVisualPulseService.class, resourceNodeVisualPulseService);
        registerSingleton(ICastleSpawnService.class, castleSpawnService);
        registerSingleton(PopulationDisplayGateway.class, populationDisplayService);
        registerSingleton(IPlayerTeleportService.class, playerTeleportService);
        registerSingleton(ICastlePlacementService.class, castlePlacementService);
        registerSingleton(ICastlePromptLaneService.class, castlePromptLaneService);
        registerSingleton(IResourceNodePromptLaneService.class, resourceNodePromptLaneService);
        registerSingleton(IUiPageRegistry.class, pageRegistry);
        registerSingleton(IUiNavigator.class, uiNavigator);
        registerSingleton(IResourceService.class, resourceService);
        registerSingleton(IPopulationService.class, populationService);
        registerSingleton(IInteriorInstanceService.class, interiorInstanceService);
        registerSingleton(IInteriorWorldService.class, interiorWorldService);
        registerSingleton(IUiActionService.class, uiActionService);
        registerSingleton(IFarmsteadMenuService.class, farmsteadMenuService);
        registerSingleton(FarmsteadStewardSpawner.class, farmsteadStewardSpawner);
        registerSingleton(IIpHashService.class, ipHashService);
        registerSingleton(IKingdomClockService.class, clockService);
        registerSingleton(IPlayerDataService.class, playerDataService);
        registerSingleton(IVisualVerificationControlHandler.class, visualVerificationControlService);
        registerSingleton(ICastleInteractionService.class, castleInteractionService);
        registerSingleton(IFocusedWorldOverrideService.class, focusedWorldOverrideService);
        registerSingleton(IFocusedWorldInteractionService.class, focusedWorldInteractionService);
        registerSingleton(ICastleProximityPromptService.class, castleProximityPromptService);
        registerSingleton(IPlacementPreviewService.class, placementPreviewService);
        registerSingleton(IPlacementModeService.class, placementModeService);
        registerSingleton(IPlacementInteractionService.class, placementInteractionService);
        registerSingleton(IResourceNodeInteractionService.class, resourceNodeInteractionService);
        registerSingleton(IBuildingInteractionService.class, buildingInteractionService);
        registerSingleton(ICustomEntitySpawnService.class, customEntitySpawnService);
        registerSingleton(IWorkerNpcInteractionService.class, workerNpcInteractionService);
        registerSingleton(KingdomBuildingCommandSupport.class, buildingCommandSupport);
        registerSingleton(KingdomNodeCommandSupport.class, nodeCommandSupport);
        registerSingleton(KingdomPlacementCommandSupport.class, placementCommandSupport);
        registerSingleton(KingdomInteractionCommandSupport.class, interactionCommandSupport);
        registerSingleton(KingdomHologramCommandSupport.class, hologramCommandSupport);
        registerSingleton(KingdomEntityCommandSupport.class, entityCommandSupport);
        registerSingleton(IFrontendCommandVerificationService.class, frontendCommandVerificationService);
        registerSingleton(IDebugCommandService.class, debugCommandService);
        registerSingleton(IInfrastructureHealthService.class, infrastructureHealthService);
        registerSingleton(WorldLabelService.class, worldLabelService);
        registerSingleton(StructureProtectionService.class, structureProtectionService);
        registerSingleton(IProtectedBlockSystemService.class, protectedBlockSystemService);
    }

    private void registerUiPages(
            IUiPageRegistry registry,
            IUiActionService actionService,
            IInfrastructureHealthService infrastructureHealthService,
            IPlayerGameStateService gameStateService,
            CastleEconomyPlanner economyPlanner,
            IResourceNodeService resourceNodeService,
            ICastleBuildingService buildingService
    ) {
        registry.register(UiPageType.CASTLE_MAIN, (player, context, state) -> new CastleMainPage(player, context, state, actionService, economyPlanner));
        registry.register(UiPageType.CASTLE_INFO, (player, context, state) -> new CastleInfoPage(player, context, state, actionService));
        registry.register(UiPageType.CASTLE_CITIZENS, (player, context, state) -> new CastleCitizensPage(player, context, state, actionService, economyPlanner));
        registry.register(UiPageType.CASTLE_TROOPS, (player, context, state) -> new CastleTroopsPage(player, context, state, actionService));
        registry.register(UiPageType.CASTLE_RESOURCES, (player, context, state) -> new CastleResourcesPage(player, context, state, actionService, economyPlanner));
        registry.register(UiPageType.CASTLE_UPGRADES, (player, context, state) -> new CastleUpgradesPage(player, context, state, actionService));
        registry.register(UiPageType.CASTLE_BUILDINGS, (player, context, state) -> new CastleBuildingsPage(player, context, state, actionService, buildingService));
        registry.register(UiPageType.FARMSTEAD_MENU, (player, context, state) -> new FarmsteadMenuPage(player, context, state, actionService, buildingService));
        registry.register(UiPageType.RESOURCE_NODE_DETAIL, (player, context, state) -> new ResourceNodePage(player, context, state, actionService, resourceNodeService));
        registry.register(UiPageType.BUILDING_DETAIL, (player, context, state) -> new BuildingDetailPage(player, context, state, actionService, buildingService));
        registry.register(UiPageType.INTERIOR_MAIN, (player, context, state) -> new InteriorMainPage(player, context, state, actionService));
        registry.register(
                UiPageType.DEBUG_NAVIGATOR,
                (player, context, state) -> new DebugNavigatorPage(
                        player,
                        context,
                        state,
                        actionService,
                        infrastructureHealthService,
                        gameStateService
                )
        );
        registry.register(
                UiPageType.DEBUG_PLACEMENT,
                (player, context, state) -> new DebugCommandPage(
                        player,
                        context,
                        state,
                        actionService,
                        infrastructureHealthService,
                        gameStateService,
                        "Pages/debug-placement.html",
                        DebugUiCommandBindings.placement()
                )
        );
        registry.register(
                UiPageType.DEBUG_INTERIOR,
                (player, context, state) -> new DebugCommandPage(
                        player,
                        context,
                        state,
                        actionService,
                        infrastructureHealthService,
                        gameStateService,
                        "Pages/debug-interior.html",
                        DebugUiCommandBindings.interior()
                )
        );
        registry.register(
                UiPageType.DEBUG_BUILDINGS,
                (player, context, state) -> new DebugCommandPage(
                        player,
                        context,
                        state,
                        actionService,
                        infrastructureHealthService,
                        gameStateService,
                        "Pages/debug-buildings.html",
                        DebugUiCommandBindings.buildings()
                )
        );
        registry.register(
                UiPageType.DEBUG_WORLD,
                (player, context, state) -> new DebugCommandPage(
                        player,
                        context,
                        state,
                        actionService,
                        infrastructureHealthService,
                        gameStateService,
                        "Pages/debug-world.html",
                        DebugUiCommandBindings.world()
                )
        );
    }

    private <T> void registerSingleton(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
    }
}
