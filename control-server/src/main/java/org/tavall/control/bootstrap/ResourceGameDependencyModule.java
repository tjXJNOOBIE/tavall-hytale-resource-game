package org.tavall.control.bootstrap;
import org.tavall.control.player.PlayerSession;

import org.tavall.control.ResourceGamePlugin;
import org.tavall.control.clock.KingdomClockHandler;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.config.CastleAssetConfig;
import org.tavall.control.config.DatabaseConfig;
import org.tavall.control.config.KingdomClockConfig;
import org.tavall.control.config.PopulationDisplayConfig;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import org.tavall.control.building.IBuildingInteractionService;
import org.tavall.control.castle.ICastleBuildingService;
import org.tavall.control.castle.ICastleBuildingVisualService;
import org.tavall.control.castle.ICastleInteractionService;
import org.tavall.control.castle.ICastleEconomySimulationService;
import org.tavall.control.castle.ICastlePromptLaneService;
import org.tavall.control.castle.ICastleProximityPromptService;
import org.tavall.control.castle.ICastlePlacementService;
import org.tavall.control.castle.ICastleSiteVisualService;
import org.tavall.control.castle.ICastleSpawnService;
import org.tavall.control.runtime.ICustomEntitySpawnService;
import org.tavall.control.runtime.IDebugCommandService;
import org.tavall.control.world.IFocusedWorldInteractionService;
import org.tavall.control.world.IFocusedWorldOverrideService;
import org.tavall.control.farmstead.ui.IFarmsteadMenuService;
import org.tavall.control.runtime.IFrontendCommandVerificationService;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.IFrontendControlConfig;
import org.tavall.control.runtime.IInfrastructureHealthService;
import org.tavall.control.interior.IInteriorInstanceService;
import org.tavall.control.interior.IInteriorWorldService;
import org.tavall.control.player.IIpHashService;
import org.tavall.control.clock.IKingdomClockService;
import org.tavall.control.building.IPlacementInteractionService;
import org.tavall.control.building.IPlacementModeService;
import org.tavall.control.building.IPlacementPreviewService;
import org.tavall.control.player.IPlayerDataService;
import org.tavall.control.player.IPlayerGameStateService;
import org.tavall.control.player.IPlayerProfileService;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.player.IPlayerTeleportService;
import org.tavall.control.population.IPopulationService;
import org.tavall.control.protection.IProtectedBlockSystemService;
import org.tavall.control.resource.IResourceNodeInteractionService;
import org.tavall.control.resource.IResourceNodePromptLaneService;
import org.tavall.control.resource.IResourceNodeService;
import org.tavall.control.resource.IResourceNodeVisualPulseService;
import org.tavall.control.resource.IResourceNodeVisualService;
import org.tavall.control.resource.IResourceService;
import org.tavall.control.ui.IUiActionService;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.ui.IUiPageRegistry;
import org.tavall.control.visual.IVisualVerificationControlHandler;
import org.tavall.control.npc.IWorkerNpcInteractionService;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.farmstead.npc.FarmsteadStewardSpawner;
import org.tavall.control.farmstead.ui.FarmsteadMenuService;
import org.tavall.control.interior.InteriorLayoutService;
import org.tavall.control.interior.InteriorStructureService;
import org.tavall.control.persistence.InMemoryPlayerGameStateStore;
import org.tavall.control.persistence.InMemoryPlayerProfileStore;
import org.tavall.control.persistence.PersistenceStoreBootstrap;
import org.tavall.control.persistence.PlayerGameStateRepository;
import org.tavall.control.persistence.PlayerGameStateStore;
import org.tavall.control.persistence.PlayerProfileRepository;
import org.tavall.control.persistence.PlayerProfileStore;
import org.tavall.control.persistence.PostgresConnectionProvider;
import org.tavall.control.persistence.ResolvedPersistenceStores;
import org.tavall.control.player.cache.PlayerProfileCache;
import org.tavall.control.player.cache.PlayerGameStateCache;
import org.tavall.control.population.PromotionCost;
import org.tavall.control.castle.CastleInteractionService;
import org.tavall.control.building.BuildingInteractionService;
import org.tavall.control.building.BuildingPlacementPlanner;
import org.tavall.control.castle.CastleBuildingService;
import org.tavall.control.castle.CastleBuildingVisualService;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.castle.CastleEconomySimulationService;
import org.tavall.control.castle.CastlePromptLaneService;
import org.tavall.control.castle.CastleProximityPromptService;
import org.tavall.control.castle.CastlePlacementService;
import org.tavall.control.castle.CastleSiteScenePlanner;
import org.tavall.control.castle.CastleSiteVisualService;
import org.tavall.control.castle.CastleSpawnService;
import org.tavall.control.runtime.CustomEntitySpawnService;
import org.tavall.control.runtime.DebugCommandService;
import org.tavall.control.world.FocusedWorldInteractionService;
import org.tavall.control.world.FocusedWorldOverrideService;
import org.tavall.control.world.FocusedWorldTargetPlanner;
import org.tavall.control.runtime.FrontendCommandVerificationHandler;
import org.tavall.control.transport.FrontendControlConfig;
import org.tavall.control.transport.FrontendTcpControlCommandClient;
import org.tavall.control.interior.InteriorInstanceService;
import org.tavall.control.interior.InteriorTourMarkerService;
import org.tavall.control.interior.InteriorWorldService;
import org.tavall.control.player.IpHashService;
import org.tavall.control.runtime.InfrastructureHealthService;
import org.tavall.control.runtime.InfrastructureMetricsRecorder;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.npc.NpcRoleResolver;
import org.tavall.control.npc.NpcVisualSpawner;
import org.tavall.control.building.PlacementInteractionService;
import org.tavall.control.building.PlacementModeService;
import org.tavall.control.building.PlacementPreviewService;
import org.tavall.control.player.PlayerDataService;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.player.PlayerProfileService;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerTeleportService;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.population.PopulationDisplayService;
import org.tavall.control.protection.ProtectedBlockSystemService;
import org.tavall.control.population.PopulationService;
import org.tavall.control.resource.ResourceNodeInteractionService;
import org.tavall.control.resource.ResourceNodePromptLaneService;
import org.tavall.control.resource.ResourceNodeRoutePlanner;
import org.tavall.control.resource.ResourceNodeService;
import org.tavall.control.resource.ResourceNodeVisualPulseService;
import org.tavall.control.resource.ResourceNodeVisualService;
import org.tavall.control.resource.ResourceService;
import org.tavall.control.protection.StructureProtectionService;
import org.tavall.control.visual.VisualVerificationControlService;
import org.tavall.control.npc.WorkerNpcInteractionService;
import org.tavall.control.world.WorldLabelService;
import org.tavall.control.ui.CastleCitizensPage;
import org.tavall.control.ui.CastleBuildingsPage;
import org.tavall.control.ui.CastleInfoPage;
import org.tavall.control.ui.CastleMainPage;
import org.tavall.control.ui.CastleResourcesPage;
import org.tavall.control.ui.CastleTroopsPage;
import org.tavall.control.ui.CastleUpgradesPage;
import org.tavall.control.ui.BuildingDetailPage;
import org.tavall.control.ui.DebugCommandPage;
import org.tavall.control.ui.DebugNavigatorPage;
import org.tavall.control.ui.DebugUiCommandBindings;
import org.tavall.control.ui.FarmsteadMenuPage;
import org.tavall.control.ui.InteriorMainPage;
import org.tavall.control.ui.ResourceNodePage;
import org.tavall.control.ui.UiActionService;
import org.tavall.control.ui.UiNavigator;
import org.tavall.control.ui.UiPageRegistry;
import org.tavall.control.ui.UiPageType;
import org.tavall.control.world.CastleEntityRegistry;
import org.tavall.control.world.CastleBuildingStructureService;
import org.tavall.control.world.BuildingPlacementStageStructureService;
import org.tavall.control.world.CastleSiteLayoutService;
import org.tavall.control.world.CastleSiteStructureService;
import org.tavall.control.world.CastlePromptLaneLayoutService;
import org.tavall.control.world.CastlePromptLaneStructureService;
import org.tavall.control.world.ResourceNodePromptLaneLayoutService;
import org.tavall.control.world.ResourceNodePromptLaneStructureService;
import org.tavall.control.world.ResourceNodeStructureService;
import org.tavall.control.commands.KingdomInteractionCommandSupport;
import org.tavall.control.commands.KingdomBuildingCommandSupport;
import org.tavall.control.commands.KingdomNodeCommandSupport;
import org.tavall.control.commands.KingdomPlacementCommandSupport;
import org.tavall.control.commands.KingdomHologramCommandSupport;
import org.tavall.control.commands.KingdomEntityCommandSupport;
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
        PlayerProfileCache profileCache = PlayerProfileCache.open(cacheConfig, mapperProvider.mapper());
        PlayerGameStateCache gameStateCache = PlayerGameStateCache.open(cacheConfig, mapperProvider.mapper());

        PersistenceStoreBootstrap persistenceBootstrap = new PersistenceStoreBootstrap(Logger.getLogger(ResourceGameDependencyModule.class.getName()));
        ResolvedPersistenceStores persistenceStores = persistenceBootstrap.resolve(databaseConfig);
        PlayerProfileStore profileStore = persistenceStores.profileStore();
        PlayerGameStateStore gameStateStore = persistenceStores.gameStateStore();

        PlayerSessionStore sessionStore = new PlayerSessionStore();
        PlayerProfileService profileService = new PlayerProfileService(
                profileStore,
                profileCache,
                InfrastructureMetricsRecorder.defaultRecorder()
        );
        PlayerGameStateService gameStateService = new PlayerGameStateService(
                gameStateStore,
                gameStateCache,
                mapperProvider.mapper(),
                InfrastructureMetricsRecorder.defaultRecorder()
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

        KingdomClockHandler clockService = new KingdomClockHandler(clockConfig);
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
        registerSingleton(PlayerProfileCache.class, profileCache);
        registerSingleton(PlayerGameStateCache.class, gameStateCache);
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



