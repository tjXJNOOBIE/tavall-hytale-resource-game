package org.tavall.control.bootstrap;
import org.tavall.control.player.PlayerGameStateService;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.control.commands.KingdomBuildingCommandSupport;
import org.tavall.control.commands.KingdomEntityCommandSupport;
import org.tavall.control.commands.KingdomHologramCommandSupport;
import org.tavall.control.commands.KingdomInteractionCommandSupport;
import org.tavall.control.commands.KingdomNodeCommandSupport;
import org.tavall.control.commands.KingdomPlacementCommandSupport;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.building.IBuildingInteractionService;
import org.tavall.control.castle.ICastleBuildingService;
import org.tavall.control.castle.ICastleBuildingVisualService;
import org.tavall.control.castle.ICastleInteractionService;
import org.tavall.control.castle.ICastleEconomySimulationService;
import org.tavall.control.castle.ICastlePlacementService;
import org.tavall.control.castle.ICastlePromptLaneService;
import org.tavall.control.castle.ICastleProximityPromptService;
import org.tavall.control.castle.ICastleSiteVisualService;
import org.tavall.control.castle.ICastleSpawnService;
import org.tavall.control.runtime.ICustomEntitySpawnService;
import org.tavall.control.runtime.IDebugCommandService;
import org.tavall.control.farmstead.ui.IFarmsteadMenuService;
import org.tavall.control.world.IFocusedWorldInteractionService;
import org.tavall.control.world.IFocusedWorldOverrideService;
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
import org.tavall.control.resource.IResourceNodePromptLaneService;
import org.tavall.control.resource.IResourceNodeService;
import org.tavall.control.resource.IResourceNodeInteractionService;
import org.tavall.control.resource.IResourceNodeVisualPulseService;
import org.tavall.control.resource.IResourceNodeVisualService;
import org.tavall.control.resource.IResourceService;
import org.tavall.control.ui.IUiActionService;
import org.tavall.control.ui.IUiNavigator;
import org.tavall.control.ui.IUiPageRegistry;
import org.tavall.control.visual.IVisualVerificationControlHandler;
import org.tavall.control.npc.IWorkerNpcInteractionService;
import org.tavall.control.interior.InteriorLayoutService;
import org.tavall.control.building.BuildingPlacementPlanner;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.world.WorldLabelService;
import org.tavall.control.world.BuildingPlacementStageStructureService;

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


