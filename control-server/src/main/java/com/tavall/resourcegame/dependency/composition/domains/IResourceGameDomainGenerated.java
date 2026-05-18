package org.tavall.control.dependency.composition.domains;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.control.commands.KingdomBuildingCommandSupport;
import org.tavall.control.commands.KingdomEntityCommandSupport;
import org.tavall.control.commands.KingdomHologramCommandSupport;
import org.tavall.control.commands.KingdomInteractionCommandSupport;
import org.tavall.control.commands.KingdomNodeCommandSupport;
import org.tavall.control.commands.KingdomPlacementCommandSupport;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.dependency.interfaces.IBuildingInteractionService;
import org.tavall.control.dependency.interfaces.ICastleBuildingService;
import org.tavall.control.dependency.interfaces.ICastleBuildingVisualService;
import org.tavall.control.dependency.interfaces.ICastleInteractionService;
import org.tavall.control.dependency.interfaces.ICastleEconomySimulationService;
import org.tavall.control.dependency.interfaces.ICastlePlacementService;
import org.tavall.control.dependency.interfaces.ICastlePromptLaneService;
import org.tavall.control.dependency.interfaces.ICastleProximityPromptService;
import org.tavall.control.dependency.interfaces.ICastleSiteVisualService;
import org.tavall.control.dependency.interfaces.ICastleSpawnService;
import org.tavall.control.dependency.interfaces.ICustomEntitySpawnService;
import org.tavall.control.dependency.interfaces.IDebugCommandService;
import org.tavall.control.dependency.interfaces.IFarmsteadMenuService;
import org.tavall.control.dependency.interfaces.IFocusedWorldInteractionService;
import org.tavall.control.dependency.interfaces.IFocusedWorldOverrideService;
import org.tavall.control.dependency.interfaces.IFrontendCommandVerificationService;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.IFrontendControlConfig;
import org.tavall.control.dependency.interfaces.IInfrastructureHealthService;
import org.tavall.control.dependency.interfaces.IInteriorInstanceService;
import org.tavall.control.dependency.interfaces.IInteriorWorldService;
import org.tavall.control.dependency.interfaces.IIpHashService;
import org.tavall.control.dependency.interfaces.IKingdomClockService;
import org.tavall.control.dependency.interfaces.IPlacementInteractionService;
import org.tavall.control.dependency.interfaces.IPlacementModeService;
import org.tavall.control.dependency.interfaces.IPlacementPreviewService;
import org.tavall.control.dependency.interfaces.IPlayerDataService;
import org.tavall.control.dependency.interfaces.IPlayerGameStateService;
import org.tavall.control.dependency.interfaces.IPlayerProfileService;
import org.tavall.control.dependency.interfaces.IPlayerSessionStore;
import org.tavall.control.dependency.interfaces.IPlayerTeleportService;
import org.tavall.control.dependency.interfaces.IPopulationService;
import org.tavall.control.dependency.interfaces.IProtectedBlockSystemService;
import org.tavall.control.dependency.interfaces.IResourceNodePromptLaneService;
import org.tavall.control.dependency.interfaces.IResourceNodeService;
import org.tavall.control.dependency.interfaces.IResourceNodeInteractionService;
import org.tavall.control.dependency.interfaces.IResourceNodeVisualPulseService;
import org.tavall.control.dependency.interfaces.IResourceNodeVisualService;
import org.tavall.control.dependency.interfaces.IResourceService;
import org.tavall.control.dependency.interfaces.IUiActionService;
import org.tavall.control.dependency.interfaces.IUiNavigator;
import org.tavall.control.dependency.interfaces.IUiPageRegistry;
import org.tavall.control.dependency.interfaces.IVisualVerificationControlHandler;
import org.tavall.control.dependency.interfaces.IWorkerNpcInteractionService;
import org.tavall.control.interior.InteriorLayoutService;
import org.tavall.control.services.BuildingPlacementPlanner;
import org.tavall.control.services.CastleEconomyPlanner;
import org.tavall.control.services.PopulationDisplayGateway;
import org.tavall.control.services.WorldLabelService;
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
