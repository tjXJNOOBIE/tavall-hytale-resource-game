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
import org.tavall.control.building.IBuildingInteractionHandler;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.castle.ICastleInteractionHandler;
import org.tavall.control.castle.ICastleEconomySimulationHandler;
import org.tavall.control.castle.ICastlePromptLaneHandler;
import org.tavall.control.castle.ICastleProximityPromptHandler;
import org.tavall.control.castle.ICastlePlacementHandler;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.castle.ICastleSpawnHandler;
import org.tavall.control.runtime.ICustomEntitySpawnHandler;
import org.tavall.control.runtime.IDebugCommandHandler;
import org.tavall.control.world.IFocusedWorldInteractionHandler;
import org.tavall.control.world.IFocusedWorldOverrideHandler;
import org.tavall.control.farmstead.ui.IFarmsteadMenuHandler;
import org.tavall.control.runtime.IFrontendCommandVerificationHandler;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.IFrontendControlConfig;
import org.tavall.control.runtime.IInfrastructureHealthHandler;
import org.tavall.control.interior.IInteriorInstanceHandler;
import org.tavall.control.interior.IInteriorWorldHandler;
import org.tavall.control.player.IIpHashHandler;
import org.tavall.control.clock.IKingdomClockHandler;
import org.tavall.control.building.IPlacementInteractionHandler;
import org.tavall.control.building.IPlacementModeHandler;
import org.tavall.control.building.IPlacementPreviewHandler;
import org.tavall.control.player.IPlayerDataHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.player.IPlayerProfileHandler;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.player.IPlayerTeleportHandler;
import org.tavall.control.population.IPopulationHandler;
import org.tavall.control.protection.IProtectedBlockSystemHandler;
import org.tavall.control.resource.IResourceNodeInteractionHandler;
import org.tavall.control.resource.IResourceNodePromptLaneHandler;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.resource.IResourceNodeVisualPulseHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.resource.IResourceHandler;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.ui.IUiPageRegistry;
import org.tavall.control.visual.IVisualVerificationControlHandler;
import org.tavall.control.npc.IWorkerNpcInteractionHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.PlayerProfile;
import org.tavall.control.farmstead.npc.FarmsteadStewardSpawner;
import org.tavall.control.farmstead.ui.FarmsteadMenuHandler;
import org.tavall.minecraft.domain.interior.InteriorLayoutHandler;
import org.tavall.control.interior.InteriorStructureHandler;
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
import org.tavall.control.castle.CastleInteractionHandler;
import org.tavall.control.building.BuildingInteractionHandler;
import org.tavall.control.building.BuildingPlacementPlanner;
import org.tavall.control.castle.CastleBuildingHandler;
import org.tavall.control.castle.CastleBuildingVisualHandler;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.castle.CastleEconomySimulationHandler;
import org.tavall.control.castle.CastlePromptLaneHandler;
import org.tavall.control.castle.CastleProximityPromptHandler;
import org.tavall.control.castle.CastlePlacementPlanner;
import org.tavall.control.castle.CastleSiteScenePlanner;
import org.tavall.control.castle.CastleSiteVisualHandler;
import org.tavall.control.castle.CastleSpawnHandler;
import org.tavall.control.runtime.CustomEntitySpawnHandler;
import org.tavall.control.runtime.DebugCommandHandler;
import org.tavall.control.world.FocusedWorldInteractionHandler;
import org.tavall.control.world.FocusedWorldOverrideHandler;
import org.tavall.control.world.FocusedWorldTargetPlanner;
import org.tavall.control.runtime.FrontendCommandVerificationHandler;
import org.tavall.control.transport.FrontendControlConfig;
import org.tavall.control.transport.FrontendTcpControlCommandClient;
import org.tavall.control.interior.InteriorInstanceHandler;
import org.tavall.control.interior.InteriorTourMarkerHandler;
import org.tavall.control.interior.InteriorWorldHandler;
import org.tavall.control.player.IpHashHandler;
import org.tavall.control.runtime.InfrastructureHealthHandler;
import org.tavall.control.runtime.InfrastructureMetricsRecorder;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.control.npc.NpcRoleResolver;
import org.tavall.control.npc.NpcVisualSpawner;
import org.tavall.control.building.PlacementInteractionHandler;
import org.tavall.control.building.PlacementModeHandler;
import org.tavall.control.building.PlacementPreviewHandler;
import org.tavall.control.player.PlayerDataHandler;
import org.tavall.control.player.PlayerGameStateHandler;
import org.tavall.control.player.PlayerProfileHandler;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerTeleportHandler;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.population.PopulationDisplayHandler;
import org.tavall.control.protection.ProtectedBlockSystemHandler;
import org.tavall.control.population.PopulationHandler;
import org.tavall.control.resource.ResourceNodeInteractionHandler;
import org.tavall.control.resource.ResourceNodePromptLaneHandler;
import org.tavall.control.resource.ResourceNodeRoutePlanner;
import org.tavall.control.resource.ResourceNodeHandler;
import org.tavall.control.resource.ResourceNodeVisualPulseHandler;
import org.tavall.control.resource.ResourceNodeVisualHandler;
import org.tavall.control.resource.ResourceHandler;
import org.tavall.control.protection.StructureProtectionHandler;
import org.tavall.control.visual.VisualVerificationControlHandler;
import org.tavall.control.npc.WorkerNpcInteractionHandler;
import org.tavall.control.world.WorldLabelHandler;
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
import org.tavall.control.ui.UiNavigator;
import org.tavall.control.ui.UiPageRegistry;
import org.tavall.control.ui.UiPageType;
import org.tavall.control.world.CastleEntityRegistry;
import org.tavall.control.world.CastleBuildingStructureHandler;
import org.tavall.control.world.BuildingPlacementStageStructureHandler;
import org.tavall.control.world.CastleSiteLayoutHandler;
import org.tavall.control.world.CastleSiteStructureHandler;
import org.tavall.control.world.CastlePromptLaneLayoutHandler;
import org.tavall.control.world.CastlePromptLaneStructureHandler;
import org.tavall.control.world.ResourceNodePromptLaneLayoutHandler;
import org.tavall.control.world.ResourceNodePromptLaneStructureHandler;
import org.tavall.control.world.ResourceNodeStructureHandler;
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
        PlayerProfileHandler profileHandler = new PlayerProfileHandler(
                profileStore,
                profileCache,
                InfrastructureMetricsRecorder.defaultRecorder()
        );
        PlayerGameStateHandler gameStateHandler = new PlayerGameStateHandler(
                gameStateStore,
                gameStateCache,
                mapperProvider.mapper(),
                InfrastructureMetricsRecorder.defaultRecorder()
        );

        CastleAssetConfig castleAssetConfig = CastleAssetConfig.defaults();
        PopulationDisplayConfig populationDisplayConfig = PopulationDisplayConfig.defaults();
        InteriorLayoutHandler interiorLayoutHandler = new InteriorLayoutHandler();
        InteriorStructureHandler interiorStructureHandler = new InteriorStructureHandler();
        InfrastructureHealthHandler infrastructureHealthHandler = new InfrastructureHealthHandler(cacheConfig, databaseConfig);
        CastleEconomyPlanner economyPlanner = new CastleEconomyPlanner();
        WorldLabelHandler worldLabelHandler = new WorldLabelHandler();
        NpcVisualSpawner npcVisualSpawner = new NpcVisualSpawner();
        NpcRoleResolver npcRoleResolver = new NpcRoleResolver();
        FarmsteadStewardSpawner farmsteadStewardSpawner = new FarmsteadStewardSpawner(npcVisualSpawner, npcRoleResolver);
        StructureProtectionHandler structureProtectionHandler = new StructureProtectionHandler();
        ProtectedBlockSystemHandler protectedBlockSystemHandler = new ProtectedBlockSystemHandler(structureProtectionHandler);
        InteriorInstanceHandler interiorInstanceHandler = new InteriorInstanceHandler();
        CastleBuildingHandler buildingHandler = new CastleBuildingHandler();
        CastleBuildingVisualHandler buildingVisualHandler = new CastleBuildingVisualHandler(
                buildingHandler,
                new CastleBuildingStructureHandler(),
                worldLabelHandler,
                structureProtectionHandler
        );
        BuildingPlacementStageStructureHandler buildingPlacementStageStructureHandler = new BuildingPlacementStageStructureHandler();
        BuildingPlacementPlanner buildingPlacementPlanner = new BuildingPlacementPlanner(
                buildingHandler,
                interiorInstanceHandler,
                gameStateHandler,
                interiorLayoutHandler
        );
        ResourceNodeHandler resourceNodeHandler = new ResourceNodeHandler(sessionStore, gameStateHandler, mapperProvider.mapper(), economyPlanner);
        ResourceNodeRoutePlanner resourceNodeRoutePlanner = new ResourceNodeRoutePlanner();
        ResourceNodeVisualHandler resourceNodeVisualHandler = new ResourceNodeVisualHandler(
                resourceNodeHandler,
                new ResourceNodeStructureHandler(),
                worldLabelHandler,
                structureProtectionHandler
        );
        ResourceNodeVisualPulseHandler resourceNodeVisualPulseHandler = new ResourceNodeVisualPulseHandler(sessionStore, resourceNodeVisualHandler);
        CastleSiteVisualHandler castleSiteVisualHandler = new CastleSiteVisualHandler(
                castleAssetConfig,
                new CastleSiteLayoutHandler(),
                new CastleSiteStructureHandler(castleAssetConfig),
                worldLabelHandler,
                structureProtectionHandler,
                sessionStore
        );

        CastleSpawnHandler castleSpawnHandler = new CastleSpawnHandler(castleAssetConfig, sessionStore, castleSiteVisualHandler);
        PopulationDisplayHandler populationDisplayHandler = new PopulationDisplayHandler(populationDisplayConfig, worldLabelHandler);
        InteriorTourMarkerHandler interiorTourMarkerHandler = new InteriorTourMarkerHandler(worldLabelHandler);
        PlayerTeleportHandler playerTeleportHandler = new PlayerTeleportHandler();
        IpHashHandler ipHashHandler = new IpHashHandler();
        PlacementPreviewHandler placementPreviewHandler = new PlacementPreviewHandler(worldLabelHandler);
        CastlePromptLaneHandler castlePromptLaneHandler = new CastlePromptLaneHandler(
                new CastlePromptLaneLayoutHandler(),
                new CastlePromptLaneStructureHandler(),
                playerTeleportHandler
        );
        ResourceNodePromptLaneHandler resourceNodePromptLaneHandler = new ResourceNodePromptLaneHandler(
                new ResourceNodePromptLaneLayoutHandler(),
                new ResourceNodePromptLaneStructureHandler(),
                playerTeleportHandler
        );
        UiPageRegistry pageRegistry = new UiPageRegistry();
        UiNavigator uiNavigator = new UiNavigator(pageRegistry);
        WorkerNpcInteractionHandler workerNpcInteractionHandler = new WorkerNpcInteractionHandler(populationDisplayHandler, sessionStore, uiNavigator);
        ResourceHandler resourceHandler = new ResourceHandler(sessionStore, gameStateHandler, castleSiteVisualHandler, uiNavigator);
        PopulationHandler populationHandler = new PopulationHandler(
                sessionStore,
                gameStateHandler,
                resourceHandler,
                castleSiteVisualHandler,
                populationDisplayHandler,
                PromotionCost.defaultCost(),
                buildingHandler,
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator
        );
        CastleEconomySimulationHandler castleEconomySimulationHandler = new CastleEconomySimulationHandler(
                sessionStore,
                gameStateHandler,
                buildingHandler,
                buildingVisualHandler,
                castleSiteVisualHandler,
                economyPlanner,
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator
        );
        CastlePlacementPlanner castlePlacementPlanner = new CastlePlacementPlanner(
                sessionStore,
                gameStateHandler,
                castleSpawnHandler,
                castleSiteVisualHandler,
                buildingVisualHandler,
                resourceNodeVisualHandler
        );
        PlacementModeHandler placementModeHandler = new PlacementModeHandler(
                sessionStore,
                placementPreviewHandler,
                buildingHandler,
                buildingVisualHandler,
                castlePlacementPlanner,
                resourceNodeHandler,
                resourceNodeVisualHandler
        );
        PlacementInteractionHandler placementInteractionHandler = new PlacementInteractionHandler(placementModeHandler);
        InteriorWorldHandler interiorWorldHandler = new InteriorWorldHandler(
                sessionStore,
                gameStateHandler,
                interiorInstanceHandler,
                interiorLayoutHandler,
                interiorStructureHandler,
                interiorTourMarkerHandler,
                playerTeleportHandler,
                populationDisplayHandler,
                buildingVisualHandler,
                uiNavigator
        );
        FarmsteadMenuHandler farmsteadMenuHandler = new FarmsteadMenuHandler(sessionStore, buildingHandler, uiNavigator);

        KingdomClockHandler clockHandler = new KingdomClockHandler(clockConfig);
        PlayerDataHandler playerDataHandler = new PlayerDataHandler(
                profileHandler,
                gameStateHandler,
                sessionStore,
                castleSpawnHandler,
                interiorInstanceHandler,
                ipHashHandler,
                clockHandler,
                resourceNodeVisualHandler,
                buildingVisualHandler,
                populationDisplayHandler,
                interiorTourMarkerHandler,
                uiNavigator
        );
        VisualVerificationControlHandler visualVerificationControlHandler = new VisualVerificationControlHandler(
                playerDataHandler,
                sessionStore,
                uiNavigator
        );
        CastleInteractionHandler castleInteractionHandler = new CastleInteractionHandler(
                sessionStore,
                uiNavigator,
                castleAssetConfig
        );
        FocusedWorldOverrideHandler focusedWorldOverrideHandler = new FocusedWorldOverrideHandler();
        FocusedWorldTargetPlanner focusedWorldTargetPlanner = new FocusedWorldTargetPlanner();
        FocusedWorldInteractionHandler focusedWorldInteractionHandler = new FocusedWorldInteractionHandler(
                sessionStore,
                buildingHandler,
                castleInteractionHandler,
                focusedWorldOverrideHandler,
                resourceNodeHandler,
                uiNavigator,
                focusedWorldTargetPlanner
        );
        CastleProximityPromptHandler castleProximityPromptHandler = new CastleProximityPromptHandler(castleInteractionHandler, placementModeHandler);
        ResourceNodeInteractionHandler resourceNodeInteractionHandler = new ResourceNodeInteractionHandler(
                sessionStore,
                resourceNodeVisualHandler,
                focusedWorldInteractionHandler,
                uiNavigator
        );
        BuildingInteractionHandler buildingInteractionHandler = new BuildingInteractionHandler(sessionStore, buildingVisualHandler, focusedWorldInteractionHandler, uiNavigator);
        CustomEntitySpawnHandler customEntitySpawnHandler = new CustomEntitySpawnHandler(
                npcVisualSpawner,
                farmsteadStewardSpawner,
                npcRoleResolver,
                sessionStore,
                buildingHandler,
                uiNavigator,
                farmsteadMenuHandler
        );
        registerUiPages(pageRegistry, infrastructureHealthHandler, gameStateHandler, populationHandler, economyPlanner, resourceNodeHandler, buildingHandler);
        KingdomPlacementCommandSupport placementCommandSupport = new KingdomPlacementCommandSupport();
        KingdomBuildingCommandSupport buildingCommandSupport = new KingdomBuildingCommandSupport(
                buildingHandler,
                buildingVisualHandler,
                uiNavigator,
                playerTeleportHandler,
                placementModeHandler,
                focusedWorldInteractionHandler,
                buildingPlacementPlanner,
                buildingPlacementStageStructureHandler
        );
        KingdomNodeCommandSupport nodeCommandSupport = new KingdomNodeCommandSupport(
                resourceNodeHandler,
                resourceNodeVisualHandler,
                uiNavigator,
                playerTeleportHandler,
                placementModeHandler,
                resourceNodePromptLaneHandler,
                focusedWorldInteractionHandler,
                focusedWorldOverrideHandler
        );
        KingdomInteractionCommandSupport interactionCommandSupport = new KingdomInteractionCommandSupport();
        KingdomHologramCommandSupport hologramCommandSupport = new KingdomHologramCommandSupport();
        KingdomEntityCommandSupport entityCommandSupport = new KingdomEntityCommandSupport();
        FrontendControlConfig frontendControlConfig = FrontendControlConfig.fromEnvironment(System.getenv());
        FrontendCommandVerificationHandler frontendCommandVerificationHandler = new FrontendCommandVerificationHandler();
        DebugCommandHandler debugCommandHandler = new DebugCommandHandler(
                sessionStore,
                uiNavigator,
                populationHandler,
                resourceHandler,
                interiorWorldHandler,
                castleSpawnHandler,
                castlePromptLaneHandler,
                focusedWorldOverrideHandler,
                playerDataHandler,
                gameStateHandler,
                infrastructureHealthHandler,
                buildingHandler,
                buildingVisualHandler,
                resourceNodeHandler,
                resourceNodeVisualHandler,
                castleSiteVisualHandler,
                castleEconomySimulationHandler,
                playerTeleportHandler,
                placementModeHandler,
                buildingCommandSupport,
                nodeCommandSupport,
                placementCommandSupport,
                interactionCommandSupport,
                hologramCommandSupport,
                entityCommandSupport,
                frontendCommandVerificationHandler
        );

        registerSingleton(IPlayerProfileHandler.class, profileHandler);
        registerSingleton(PlayerProfileCache.class, profileCache);
        registerSingleton(PlayerGameStateCache.class, gameStateCache);
        registerSingleton(com.fasterxml.jackson.databind.ObjectMapper.class, mapperProvider.mapper());
        registerSingleton(IFrontendControlConfig.class, frontendControlConfig);
        registerSingleton(IFrontendControlCommandClient.class, new FrontendTcpControlCommandClient());
        registerSingleton(IPlayerGameStateHandler.class, gameStateHandler);
        registerSingleton(IPlayerSessionStore.class, sessionStore);
        registerSingleton(ICastleBuildingHandler.class, buildingHandler);
        registerSingleton(ICastleBuildingVisualHandler.class, buildingVisualHandler);
        registerSingleton(InteriorLayoutHandler.class, interiorLayoutHandler);
        registerSingleton(CastleEconomyPlanner.class, economyPlanner);
        registerSingleton(BuildingPlacementStageStructureHandler.class, buildingPlacementStageStructureHandler);
        registerSingleton(BuildingPlacementPlanner.class, buildingPlacementPlanner);
        registerSingleton(ICastleEconomySimulationHandler.class, castleEconomySimulationHandler);
        registerSingleton(ICastleSiteVisualHandler.class, castleSiteVisualHandler);
        registerSingleton(IResourceNodeHandler.class, resourceNodeHandler);
        registerSingleton(IResourceNodeVisualHandler.class, resourceNodeVisualHandler);
        registerSingleton(IResourceNodeVisualPulseHandler.class, resourceNodeVisualPulseHandler);
        registerSingleton(ICastleSpawnHandler.class, castleSpawnHandler);
        registerSingleton(PopulationDisplayGateway.class, populationDisplayHandler);
        registerSingleton(IPlayerTeleportHandler.class, playerTeleportHandler);
        registerSingleton(ICastlePlacementHandler.class, castlePlacementPlanner);
        registerSingleton(ICastlePromptLaneHandler.class, castlePromptLaneHandler);
        registerSingleton(IResourceNodePromptLaneHandler.class, resourceNodePromptLaneHandler);
        registerSingleton(IUiPageRegistry.class, pageRegistry);
        registerSingleton(IUiNavigator.class, uiNavigator);
        registerSingleton(IResourceHandler.class, resourceHandler);
        registerSingleton(IPopulationHandler.class, populationHandler);
        registerSingleton(IInteriorInstanceHandler.class, interiorInstanceHandler);
        registerSingleton(IInteriorWorldHandler.class, interiorWorldHandler);
        registerSingleton(IFarmsteadMenuHandler.class, farmsteadMenuHandler);
        registerSingleton(FarmsteadStewardSpawner.class, farmsteadStewardSpawner);
        registerSingleton(IIpHashHandler.class, ipHashHandler);
        registerSingleton(IKingdomClockHandler.class, clockHandler);
        registerSingleton(IPlayerDataHandler.class, playerDataHandler);
        registerSingleton(IVisualVerificationControlHandler.class, visualVerificationControlHandler);
        registerSingleton(ICastleInteractionHandler.class, castleInteractionHandler);
        registerSingleton(IFocusedWorldOverrideHandler.class, focusedWorldOverrideHandler);
        registerSingleton(IFocusedWorldInteractionHandler.class, focusedWorldInteractionHandler);
        registerSingleton(ICastleProximityPromptHandler.class, castleProximityPromptHandler);
        registerSingleton(IPlacementPreviewHandler.class, placementPreviewHandler);
        registerSingleton(IPlacementModeHandler.class, placementModeHandler);
        registerSingleton(IPlacementInteractionHandler.class, placementInteractionHandler);
        registerSingleton(IResourceNodeInteractionHandler.class, resourceNodeInteractionHandler);
        registerSingleton(IBuildingInteractionHandler.class, buildingInteractionHandler);
        registerSingleton(ICustomEntitySpawnHandler.class, customEntitySpawnHandler);
        registerSingleton(IWorkerNpcInteractionHandler.class, workerNpcInteractionHandler);
        registerSingleton(KingdomBuildingCommandSupport.class, buildingCommandSupport);
        registerSingleton(KingdomNodeCommandSupport.class, nodeCommandSupport);
        registerSingleton(KingdomPlacementCommandSupport.class, placementCommandSupport);
        registerSingleton(KingdomInteractionCommandSupport.class, interactionCommandSupport);
        registerSingleton(KingdomHologramCommandSupport.class, hologramCommandSupport);
        registerSingleton(KingdomEntityCommandSupport.class, entityCommandSupport);
        registerSingleton(IFrontendCommandVerificationHandler.class, frontendCommandVerificationHandler);
        registerSingleton(IDebugCommandHandler.class, debugCommandHandler);
        registerSingleton(IInfrastructureHealthHandler.class, infrastructureHealthHandler);
        registerSingleton(WorldLabelHandler.class, worldLabelHandler);
        registerSingleton(StructureProtectionHandler.class, structureProtectionHandler);
        registerSingleton(IProtectedBlockSystemHandler.class, protectedBlockSystemHandler);
    }

    private void registerUiPages(
            IUiPageRegistry registry,
            IInfrastructureHealthHandler infrastructureHealthHandler,
            IPlayerGameStateHandler gameStateHandler,
            IPopulationHandler populationHandler,
            CastleEconomyPlanner economyPlanner,
            IResourceNodeHandler resourceNodeHandler,
            ICastleBuildingHandler buildingHandler
    ) {
        registry.register(UiPageType.CASTLE_MAIN, (player, context, state) -> new CastleMainPage(player, context, state, economyPlanner));
        registry.register(UiPageType.CASTLE_INFO, (player, context, state) -> new CastleInfoPage(player, context, state));
        registry.register(UiPageType.CASTLE_CITIZENS, (player, context, state) -> new CastleCitizensPage(player, context, state, economyPlanner));
        registry.register(UiPageType.CASTLE_TROOPS, (player, context, state) -> new CastleTroopsPage(player, context, state));
        registry.register(UiPageType.CASTLE_RESOURCES, (player, context, state) -> new CastleResourcesPage(player, context, state, economyPlanner));
        registry.register(UiPageType.CASTLE_UPGRADES, (player, context, state) -> new CastleUpgradesPage(player, context, state, populationHandler, gameStateHandler));
        registry.register(UiPageType.CASTLE_BUILDINGS, (player, context, state) -> new CastleBuildingsPage(player, context, state, buildingHandler));
        registry.register(UiPageType.FARMSTEAD_MENU, (player, context, state) -> new FarmsteadMenuPage(player, context, state, buildingHandler));
        registry.register(UiPageType.RESOURCE_NODE_DETAIL, (player, context, state) -> new ResourceNodePage(player, context, state, resourceNodeHandler));
        registry.register(UiPageType.BUILDING_DETAIL, (player, context, state) -> new BuildingDetailPage(player, context, state, buildingHandler));
        registry.register(UiPageType.INTERIOR_MAIN, (player, context, state) -> new InteriorMainPage(player, context, state, gameStateHandler));
        registry.register(
                UiPageType.DEBUG_NAVIGATOR,
                (player, context, state) -> new DebugNavigatorPage(player, context, state, infrastructureHealthHandler, gameStateHandler)
        );
        registry.register(
                UiPageType.DEBUG_PLACEMENT,
                (player, context, state) -> new DebugCommandPage(
                        player,
                        context,
                        state,
                        infrastructureHealthHandler,
                        gameStateHandler,
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
                        infrastructureHealthHandler,
                        gameStateHandler,
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
                        infrastructureHealthHandler,
                        gameStateHandler,
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
                        infrastructureHealthHandler,
                        gameStateHandler,
                        "Pages/debug-world.html",
                        DebugUiCommandBindings.world()
                )
        );
    }

    private <T> void registerSingleton(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
    }
}



