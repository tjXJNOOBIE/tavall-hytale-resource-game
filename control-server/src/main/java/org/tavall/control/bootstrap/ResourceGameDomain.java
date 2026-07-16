package org.tavall.control.bootstrap;
import org.tavall.control.player.PlayerGameStateHandler;
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
import org.tavall.control.building.IBuildingInteractionHandler;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.castle.ICastleBuildingVisualHandler;
import org.tavall.control.castle.ICastleInteractionHandler;
import org.tavall.control.castle.ICastleEconomySimulationHandler;
import org.tavall.control.castle.ICastlePlacementHandler;
import org.tavall.control.castle.ICastlePromptLaneHandler;
import org.tavall.control.castle.ICastleProximityPromptHandler;
import org.tavall.control.castle.ICastleSiteVisualHandler;
import org.tavall.control.castle.ICastleSpawnHandler;
import org.tavall.control.runtime.ICustomEntitySpawnHandler;
import org.tavall.control.runtime.IDebugCommandHandler;
import org.tavall.control.farmstead.ui.IFarmsteadMenuHandler;
import org.tavall.control.world.IFocusedWorldInteractionHandler;
import org.tavall.control.world.IFocusedWorldOverrideHandler;
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
import org.tavall.control.resource.IResourceNodePromptLaneHandler;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.resource.IResourceNodeInteractionHandler;
import org.tavall.control.resource.IResourceNodeVisualPulseHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;
import org.tavall.control.resource.IResourceHandler;
import org.tavall.control.api.UIData;
import org.tavall.control.visual.IVisualVerificationControlHandler;
import org.tavall.control.npc.IWorkerNpcInteractionHandler;
import org.tavall.minecraft.domain.interior.InteriorLayoutHandler;
import org.tavall.control.building.BuildingPlacementPlanner;
import org.tavall.control.castle.CastleEconomyPlanner;
import org.tavall.control.population.PopulationDisplayGateway;
import org.tavall.control.world.WorldLabelHandler;
import org.tavall.control.world.BuildingPlacementStageStructureHandler;

/**
 * Domain equivalent for repo-local DI accessors.
 */
public interface ResourceGameDomain {
    default <T> T registerSingleton(Class<T> type, T instance) {
        DependencyLoaderAccess.registerInstance(type, instance);
        return instance;
    }

    default IPlayerDataHandler getPlayerDataHandler() {
        return DependencyLoaderAccess.findInstance(IPlayerDataHandler.class);
    }

    default IPlayerProfileHandler getPlayerProfileHandler() {
        return DependencyLoaderAccess.findInstance(IPlayerProfileHandler.class);
    }

    default IPlayerGameStateHandler getPlayerGameStateHandler() {
        return DependencyLoaderAccess.findInstance(IPlayerGameStateHandler.class);
    }

    default IPlayerGameStateHandler registerPlayerGameStateHandler(IPlayerGameStateHandler playerGameStateHandler) {
        DependencyLoaderAccess.registerInstance(IPlayerGameStateHandler.class, playerGameStateHandler);
        return playerGameStateHandler;
    }

    default IPlayerSessionStore getPlayerSessionStore() {
        return DependencyLoaderAccess.findInstance(IPlayerSessionStore.class);
    }

    default IPlayerSessionStore registerPlayerSessionStore(IPlayerSessionStore playerSessionStore) {
        DependencyLoaderAccess.registerInstance(IPlayerSessionStore.class, playerSessionStore);
        return playerSessionStore;
    }

    default IPlayerTeleportHandler getPlayerTeleportHandler() {
        return DependencyLoaderAccess.findInstance(IPlayerTeleportHandler.class);
    }

    default ObjectMapper getObjectMapper() {
        return DependencyLoaderAccess.findInstance(ObjectMapper.class);
    }

    default ObjectMapper registerObjectMapper(ObjectMapper objectMapper) {
        DependencyLoaderAccess.registerInstance(ObjectMapper.class, objectMapper);
        return objectMapper;
    }

    default ICastleInteractionHandler getCastleInteractionHandler() {
        return DependencyLoaderAccess.findInstance(ICastleInteractionHandler.class);
    }

    default ICastleBuildingHandler getCastleBuildingHandler() {
        return DependencyLoaderAccess.findInstance(ICastleBuildingHandler.class);
    }

    default ICastleBuildingVisualHandler getCastleBuildingVisualHandler() {
        return DependencyLoaderAccess.findInstance(ICastleBuildingVisualHandler.class);
    }

    default ICastlePlacementHandler getCastlePlacementHandler() {
        return DependencyLoaderAccess.findInstance(ICastlePlacementHandler.class);
    }

    default ICastlePromptLaneHandler getCastlePromptLaneHandler() {
        return DependencyLoaderAccess.findInstance(ICastlePromptLaneHandler.class);
    }

    default ICastleSiteVisualHandler getCastleSiteVisualHandler() {
        return DependencyLoaderAccess.findInstance(ICastleSiteVisualHandler.class);
    }

    default ICastleSpawnHandler getCastleSpawnHandler() {
        return DependencyLoaderAccess.findInstance(ICastleSpawnHandler.class);
    }

    default ICastleProximityPromptHandler getCastleProximityPromptHandler() {
        return DependencyLoaderAccess.findInstance(ICastleProximityPromptHandler.class);
    }

    default ICastleEconomySimulationHandler getCastleEconomySimulationHandler() {
        return DependencyLoaderAccess.findInstance(ICastleEconomySimulationHandler.class);
    }

    default CastleEconomyPlanner getCastleEconomyPlanner() {
        return DependencyLoaderAccess.findInstance(CastleEconomyPlanner.class);
    }

    default IResourceNodeInteractionHandler getResourceNodeInteractionHandler() {
        return DependencyLoaderAccess.findInstance(IResourceNodeInteractionHandler.class);
    }

    default IResourceNodeHandler getResourceNodeHandler() {
        return DependencyLoaderAccess.findInstance(IResourceNodeHandler.class);
    }

    default IResourceNodeVisualHandler getResourceNodeVisualHandler() {
        return DependencyLoaderAccess.findInstance(IResourceNodeVisualHandler.class);
    }

    default IResourceNodePromptLaneHandler getResourceNodePromptLaneHandler() {
        return DependencyLoaderAccess.findInstance(IResourceNodePromptLaneHandler.class);
    }

    default IWorkerNpcInteractionHandler getWorkerNpcInteractionHandler() {
        return DependencyLoaderAccess.findInstance(IWorkerNpcInteractionHandler.class);
    }

    default ICustomEntitySpawnHandler getCustomEntitySpawnHandler() {
        return DependencyLoaderAccess.findInstance(ICustomEntitySpawnHandler.class);
    }

    default IBuildingInteractionHandler getBuildingInteractionHandler() {
        return DependencyLoaderAccess.findInstance(IBuildingInteractionHandler.class);
    }

    default BuildingPlacementPlanner getBuildingPlacementPlanner() {
        return DependencyLoaderAccess.findInstance(BuildingPlacementPlanner.class);
    }

    default BuildingPlacementStageStructureHandler getBuildingPlacementStageStructureHandler() {
        return DependencyLoaderAccess.findInstance(BuildingPlacementStageStructureHandler.class);
    }

    default IPlacementInteractionHandler getPlacementInteractionHandler() {
        return DependencyLoaderAccess.findInstance(IPlacementInteractionHandler.class);
    }

    default IResourceNodeVisualPulseHandler getResourceNodeVisualPulseHandler() {
        return DependencyLoaderAccess.findInstance(IResourceNodeVisualPulseHandler.class);
    }

    default IDebugCommandHandler getDebugCommandHandler() {
        return DependencyLoaderAccess.findInstance(IDebugCommandHandler.class);
    }

    default IInteriorInstanceHandler getInteriorInstanceHandler() {
        return DependencyLoaderAccess.findInstance(IInteriorInstanceHandler.class);
    }

    default IInteriorInstanceHandler registerInteriorInstanceHandler(IInteriorInstanceHandler interiorInstanceHandler) {
        DependencyLoaderAccess.registerInstance(IInteriorInstanceHandler.class, interiorInstanceHandler);
        return interiorInstanceHandler;
    }

    default IInteriorWorldHandler getInteriorWorldHandler() {
        return DependencyLoaderAccess.findInstance(IInteriorWorldHandler.class);
    }

    default InteriorLayoutHandler getInteriorLayoutHandler() {
        return DependencyLoaderAccess.findInstance(InteriorLayoutHandler.class);
    }

    default InteriorLayoutHandler registerInteriorLayoutHandler(InteriorLayoutHandler interiorLayoutHandler) {
        DependencyLoaderAccess.registerInstance(InteriorLayoutHandler.class, interiorLayoutHandler);
        return interiorLayoutHandler;
    }

    default IKingdomClockHandler getKingdomClockHandler() {
        return DependencyLoaderAccess.findInstance(IKingdomClockHandler.class);
    }

    default IResourceHandler getResourceHandler() {
        return DependencyLoaderAccess.findInstance(IResourceHandler.class);
    }

    default IPopulationHandler getPopulationHandler() {
        return DependencyLoaderAccess.findInstance(IPopulationHandler.class);
    }

    default PopulationDisplayGateway getPopulationDisplayGateway() {
        return DependencyLoaderAccess.findInstance(PopulationDisplayGateway.class);
    }

    default UIData getUIData() {
        return DependencyLoaderAccess.findInstance(UIData.class);
    }

    default IFarmsteadMenuHandler getFarmsteadMenuHandler() {
        return DependencyLoaderAccess.findInstance(IFarmsteadMenuHandler.class);
    }

    default IFocusedWorldOverrideHandler getFocusedWorldOverrideHandler() {
        return DependencyLoaderAccess.findInstance(IFocusedWorldOverrideHandler.class);
    }

    default IFocusedWorldInteractionHandler getFocusedWorldInteractionHandler() {
        return DependencyLoaderAccess.findInstance(IFocusedWorldInteractionHandler.class);
    }

    default IIpHashHandler getIpHashHandler() {
        return DependencyLoaderAccess.findInstance(IIpHashHandler.class);
    }

    default IInfrastructureHealthHandler getInfrastructureHealthHandler() {
        return DependencyLoaderAccess.findInstance(IInfrastructureHealthHandler.class);
    }

    default IPlacementPreviewHandler getPlacementPreviewHandler() {
        return DependencyLoaderAccess.findInstance(IPlacementPreviewHandler.class);
    }

    default IPlacementModeHandler getPlacementModeHandler() {
        return DependencyLoaderAccess.findInstance(IPlacementModeHandler.class);
    }

    default IProtectedBlockSystemHandler getProtectedBlockSystemHandler() {
        return DependencyLoaderAccess.findInstance(IProtectedBlockSystemHandler.class);
    }

    default IVisualVerificationControlHandler getVisualVerificationControlHandler() {
        return DependencyLoaderAccess.findInstance(IVisualVerificationControlHandler.class);
    }

    default IFrontendCommandVerificationHandler getFrontendCommandVerificationHandler() {
        return DependencyLoaderAccess.findInstance(IFrontendCommandVerificationHandler.class);
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

    default WorldLabelHandler getWorldLabelHandler() {
        return DependencyLoaderAccess.findInstance(WorldLabelHandler.class);
    }
}
