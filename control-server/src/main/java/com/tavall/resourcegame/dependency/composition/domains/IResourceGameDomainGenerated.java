package com.tavall.resourcegame.dependency.composition.domains;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.commands.KingdomBuildingCommandSupport;
import com.tavall.resourcegame.commands.KingdomEntityCommandSupport;
import com.tavall.resourcegame.commands.KingdomHologramCommandSupport;
import com.tavall.resourcegame.commands.KingdomInteractionCommandSupport;
import com.tavall.resourcegame.commands.KingdomNodeCommandSupport;
import com.tavall.resourcegame.commands.KingdomPlacementCommandSupport;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.dependency.interfaces.IBuildingInteractionService;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingService;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingVisualService;
import com.tavall.resourcegame.dependency.interfaces.ICastleInteractionService;
import com.tavall.resourcegame.dependency.interfaces.ICastleEconomySimulationService;
import com.tavall.resourcegame.dependency.interfaces.ICastlePlacementService;
import com.tavall.resourcegame.dependency.interfaces.ICastlePromptLaneService;
import com.tavall.resourcegame.dependency.interfaces.ICastleProximityPromptService;
import com.tavall.resourcegame.dependency.interfaces.ICastleSiteVisualService;
import com.tavall.resourcegame.dependency.interfaces.ICastleSpawnService;
import com.tavall.resourcegame.dependency.interfaces.ICustomEntitySpawnService;
import com.tavall.resourcegame.dependency.interfaces.IDebugCommandService;
import com.tavall.resourcegame.dependency.interfaces.IFarmsteadMenuService;
import com.tavall.resourcegame.dependency.interfaces.IFocusedWorldInteractionService;
import com.tavall.resourcegame.dependency.interfaces.IFocusedWorldOverrideService;
import com.tavall.resourcegame.dependency.interfaces.IFrontendCommandVerificationService;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlConfig;
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
import com.tavall.resourcegame.dependency.interfaces.IResourceNodePromptLaneService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeInteractionService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeVisualPulseService;
import com.tavall.resourcegame.dependency.interfaces.IResourceNodeVisualService;
import com.tavall.resourcegame.dependency.interfaces.IResourceService;
import com.tavall.resourcegame.dependency.interfaces.IUiActionService;
import com.tavall.resourcegame.dependency.interfaces.IUiNavigator;
import com.tavall.resourcegame.dependency.interfaces.IUiPageRegistry;
import com.tavall.resourcegame.dependency.interfaces.IVisualVerificationControlHandler;
import com.tavall.resourcegame.dependency.interfaces.IWorkerNpcInteractionService;
import com.tavall.resourcegame.interior.InteriorLayoutService;
import com.tavall.resourcegame.services.BuildingPlacementPlanner;
import com.tavall.resourcegame.services.CastleEconomyPlanner;
import com.tavall.resourcegame.services.PopulationDisplayGateway;
import com.tavall.resourcegame.services.WorldLabelService;
import com.tavall.resourcegame.world.BuildingPlacementStageStructureService;

/**
 * Generated-domain equivalent for repo-local DI accessors.
 */
public interface IResourceGameDomainGenerated {
    default IPlayerDataService getPlayerDataService() {
        return DependencyLoaderAccess.findInstance(IPlayerDataService.class);
    }

    default IPlayerProfileService getPlayerProfileService() {
        return DependencyLoaderAccess.findInstance(IPlayerProfileService.class);
    }

    default IPlayerGameStateService getPlayerGameStateService() {
        return DependencyLoaderAccess.findInstance(IPlayerGameStateService.class);
    }

    default IPlayerGameStateService registerPlayerGameStateService(IPlayerGameStateService playerGameStateService) {
        DependencyLoaderAccess.registerInstance(IPlayerGameStateService.class, playerGameStateService);
        return playerGameStateService;
    }

    default IPlayerSessionStore getPlayerSessionStore() {
        return DependencyLoaderAccess.findInstance(IPlayerSessionStore.class);
    }

    default IPlayerSessionStore registerPlayerSessionStore(IPlayerSessionStore playerSessionStore) {
        DependencyLoaderAccess.registerInstance(IPlayerSessionStore.class, playerSessionStore);
        return playerSessionStore;
    }

    default IPlayerTeleportService getPlayerTeleportService() {
        return DependencyLoaderAccess.findInstance(IPlayerTeleportService.class);
    }

    default ObjectMapper getObjectMapper() {
        return DependencyLoaderAccess.findInstance(ObjectMapper.class);
    }

    default ObjectMapper registerObjectMapper(ObjectMapper objectMapper) {
        DependencyLoaderAccess.registerInstance(ObjectMapper.class, objectMapper);
        return objectMapper;
    }

    default ICastleInteractionService getCastleInteractionService() {
        return DependencyLoaderAccess.findInstance(ICastleInteractionService.class);
    }

    default ICastleBuildingService getCastleBuildingService() {
        return DependencyLoaderAccess.findInstance(ICastleBuildingService.class);
    }

    default ICastleBuildingVisualService getCastleBuildingVisualService() {
        return DependencyLoaderAccess.findInstance(ICastleBuildingVisualService.class);
    }

    default ICastlePlacementService getCastlePlacementService() {
        return DependencyLoaderAccess.findInstance(ICastlePlacementService.class);
    }

    default ICastlePromptLaneService getCastlePromptLaneService() {
        return DependencyLoaderAccess.findInstance(ICastlePromptLaneService.class);
    }

    default ICastleSiteVisualService getCastleSiteVisualService() {
        return DependencyLoaderAccess.findInstance(ICastleSiteVisualService.class);
    }

    default ICastleSpawnService getCastleSpawnService() {
        return DependencyLoaderAccess.findInstance(ICastleSpawnService.class);
    }

    default ICastleProximityPromptService getCastleProximityPromptService() {
        return DependencyLoaderAccess.findInstance(ICastleProximityPromptService.class);
    }

    default ICastleEconomySimulationService getCastleEconomySimulationService() {
        return DependencyLoaderAccess.findInstance(ICastleEconomySimulationService.class);
    }

    default CastleEconomyPlanner getCastleEconomyPlanner() {
        return DependencyLoaderAccess.findInstance(CastleEconomyPlanner.class);
    }

    default IResourceNodeInteractionService getResourceNodeInteractionService() {
        return DependencyLoaderAccess.findInstance(IResourceNodeInteractionService.class);
    }

    default IResourceNodeService getResourceNodeService() {
        return DependencyLoaderAccess.findInstance(IResourceNodeService.class);
    }

    default IResourceNodeVisualService getResourceNodeVisualService() {
        return DependencyLoaderAccess.findInstance(IResourceNodeVisualService.class);
    }

    default IResourceNodePromptLaneService getResourceNodePromptLaneService() {
        return DependencyLoaderAccess.findInstance(IResourceNodePromptLaneService.class);
    }

    default IWorkerNpcInteractionService getWorkerNpcInteractionService() {
        return DependencyLoaderAccess.findInstance(IWorkerNpcInteractionService.class);
    }

    default ICustomEntitySpawnService getCustomEntitySpawnService() {
        return DependencyLoaderAccess.findInstance(ICustomEntitySpawnService.class);
    }

    default IBuildingInteractionService getBuildingInteractionService() {
        return DependencyLoaderAccess.findInstance(IBuildingInteractionService.class);
    }

    default BuildingPlacementPlanner getBuildingPlacementPlanner() {
        return DependencyLoaderAccess.findInstance(BuildingPlacementPlanner.class);
    }

    default BuildingPlacementStageStructureService getBuildingPlacementStageStructureService() {
        return DependencyLoaderAccess.findInstance(BuildingPlacementStageStructureService.class);
    }

    default IPlacementInteractionService getPlacementInteractionService() {
        return DependencyLoaderAccess.findInstance(IPlacementInteractionService.class);
    }

    default IResourceNodeVisualPulseService getResourceNodeVisualPulseService() {
        return DependencyLoaderAccess.findInstance(IResourceNodeVisualPulseService.class);
    }

    default IDebugCommandService getDebugCommandService() {
        return DependencyLoaderAccess.findInstance(IDebugCommandService.class);
    }

    default IInteriorInstanceService getInteriorInstanceService() {
        return DependencyLoaderAccess.findInstance(IInteriorInstanceService.class);
    }

    default IInteriorInstanceService registerInteriorInstanceService(IInteriorInstanceService interiorInstanceService) {
        DependencyLoaderAccess.registerInstance(IInteriorInstanceService.class, interiorInstanceService);
        return interiorInstanceService;
    }

    default IInteriorWorldService getInteriorWorldService() {
        return DependencyLoaderAccess.findInstance(IInteriorWorldService.class);
    }

    default InteriorLayoutService getInteriorLayoutService() {
        return DependencyLoaderAccess.findInstance(InteriorLayoutService.class);
    }

    default InteriorLayoutService registerInteriorLayoutService(InteriorLayoutService interiorLayoutService) {
        DependencyLoaderAccess.registerInstance(InteriorLayoutService.class, interiorLayoutService);
        return interiorLayoutService;
    }

    default IKingdomClockService getKingdomClockService() {
        return DependencyLoaderAccess.findInstance(IKingdomClockService.class);
    }

    default IResourceService getResourceService() {
        return DependencyLoaderAccess.findInstance(IResourceService.class);
    }

    default IPopulationService getPopulationService() {
        return DependencyLoaderAccess.findInstance(IPopulationService.class);
    }

    default PopulationDisplayGateway getPopulationDisplayGateway() {
        return DependencyLoaderAccess.findInstance(PopulationDisplayGateway.class);
    }

    default IUiNavigator getUiNavigator() {
        return DependencyLoaderAccess.findInstance(IUiNavigator.class);
    }

    default IUiPageRegistry getUiPageRegistry() {
        return DependencyLoaderAccess.findInstance(IUiPageRegistry.class);
    }

    default IUiActionService getUiActionService() {
        return DependencyLoaderAccess.findInstance(IUiActionService.class);
    }

    default IFarmsteadMenuService getFarmsteadMenuService() {
        return DependencyLoaderAccess.findInstance(IFarmsteadMenuService.class);
    }

    default IFocusedWorldOverrideService getFocusedWorldOverrideService() {
        return DependencyLoaderAccess.findInstance(IFocusedWorldOverrideService.class);
    }

    default IFocusedWorldInteractionService getFocusedWorldInteractionService() {
        return DependencyLoaderAccess.findInstance(IFocusedWorldInteractionService.class);
    }

    default IIpHashService getIpHashService() {
        return DependencyLoaderAccess.findInstance(IIpHashService.class);
    }

    default IInfrastructureHealthService getInfrastructureHealthService() {
        return DependencyLoaderAccess.findInstance(IInfrastructureHealthService.class);
    }

    default IPlacementPreviewService getPlacementPreviewService() {
        return DependencyLoaderAccess.findInstance(IPlacementPreviewService.class);
    }

    default IPlacementModeService getPlacementModeService() {
        return DependencyLoaderAccess.findInstance(IPlacementModeService.class);
    }

    default IProtectedBlockSystemService getProtectedBlockSystemService() {
        return DependencyLoaderAccess.findInstance(IProtectedBlockSystemService.class);
    }

    default IVisualVerificationControlHandler getVisualVerificationControlHandler() {
        return DependencyLoaderAccess.findInstance(IVisualVerificationControlHandler.class);
    }

    default IFrontendCommandVerificationService getFrontendCommandVerificationService() {
        return DependencyLoaderAccess.findInstance(IFrontendCommandVerificationService.class);
    }

    default IFrontendControlConfig getFrontendControlConfig() {
        return DependencyLoaderAccess.findInstance(IFrontendControlConfig.class);
    }

    default IFrontendControlCommandClient getFrontendControlCommandClient() {
        return DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class);
    }

    default KingdomBuildingCommandSupport getKingdomBuildingCommandSupport() {
        return DependencyLoaderAccess.findInstance(KingdomBuildingCommandSupport.class);
    }

    default KingdomNodeCommandSupport getKingdomNodeCommandSupport() {
        return DependencyLoaderAccess.findInstance(KingdomNodeCommandSupport.class);
    }

    default KingdomPlacementCommandSupport getKingdomPlacementCommandSupport() {
        return DependencyLoaderAccess.findInstance(KingdomPlacementCommandSupport.class);
    }

    default KingdomInteractionCommandSupport getKingdomInteractionCommandSupport() {
        return DependencyLoaderAccess.findInstance(KingdomInteractionCommandSupport.class);
    }

    default KingdomHologramCommandSupport getKingdomHologramCommandSupport() {
        return DependencyLoaderAccess.findInstance(KingdomHologramCommandSupport.class);
    }

    default KingdomEntityCommandSupport getKingdomEntityCommandSupport() {
        return DependencyLoaderAccess.findInstance(KingdomEntityCommandSupport.class);
    }

    default WorldLabelService getWorldLabelService() {
        return DependencyLoaderAccess.findInstance(WorldLabelService.class);
    }
}
