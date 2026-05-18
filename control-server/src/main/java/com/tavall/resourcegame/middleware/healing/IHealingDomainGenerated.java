package org.tavall.control.healing;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.asset.GlobalAssetRepository;
import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.troop.TroopRepository;

public interface IHealingDomainGenerated {
    default GlobalAssetRepository getGlobalAssetRepository() {
        return DependencyLoaderAccess.findInstance(GlobalAssetRepository.class);
    }

    default GlobalAssetResolutionHandler getGlobalAssetResolutionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GlobalAssetResolutionHandler.class)
                .orElseGet(() -> registerGlobalAssetResolutionHandler(new GlobalAssetResolutionHandler(getGlobalAssetRepository())));
    }

    default HealingFacilityDefinitionRegistry getHealingFacilityDefinitionRegistry() {
        return DependencyLoaderAccess.findOptionalInstance(HealingFacilityDefinitionRegistry.class)
                .orElseGet(() -> registerHealingFacilityDefinitionRegistry(new HealingFacilityDefinitionRegistry()));
    }

    default HealingFacilityModifierCalculationHandler getHealingFacilityModifierCalculationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(HealingFacilityModifierCalculationHandler.class)
                .orElseGet(() -> registerHealingFacilityModifierCalculationHandler(new HealingFacilityModifierCalculationHandler()));
    }

    default HealingInventoryRepository getHealingInventoryRepository() {
        return DependencyLoaderAccess.findInstance(HealingInventoryRepository.class);
    }

    default HealingItemCraftingHandler getHealingItemCraftingHandler() {
        return DependencyLoaderAccess.findOptionalInstance(HealingItemCraftingHandler.class)
                .orElseGet(() -> registerHealingItemCraftingHandler(new HealingItemCraftingHandler()));
    }

    default HealingResourceCostCalculationHandler getHealingResourceCostCalculationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(HealingResourceCostCalculationHandler.class)
                .orElseGet(() -> registerHealingResourceCostCalculationHandler(new HealingResourceCostCalculationHandler()));
    }

    default MedicalItemCraftingHandler getMedicalItemCraftingHandler() {
        return DependencyLoaderAccess.findOptionalInstance(MedicalItemCraftingHandler.class)
                .orElseGet(() -> registerMedicalItemCraftingHandler(new MedicalItemCraftingHandler()));
    }

    default TroopHealingCompletionHandler getTroopHealingCompletionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopHealingCompletionHandler.class)
                .orElseGet(() -> registerTroopHealingCompletionHandler(new TroopHealingCompletionHandler()));
    }

    default TroopHealingProgressTickHandler getTroopHealingProgressTickHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopHealingProgressTickHandler.class)
                .orElseGet(() -> registerTroopHealingProgressTickHandler(new TroopHealingProgressTickHandler()));
    }

    default TroopHealingProjectionHandler getTroopHealingProjectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopHealingProjectionHandler.class)
                .orElseGet(() -> registerTroopHealingProjectionHandler(new TroopHealingProjectionHandler()));
    }

    default TroopHealingRecipeSelectionHandler getTroopHealingRecipeSelectionHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopHealingRecipeSelectionHandler.class)
                .orElseGet(() -> registerTroopHealingRecipeSelectionHandler(new TroopHealingRecipeSelectionHandler()));
    }

    default TroopHealingRecipeValidationHandler getTroopHealingRecipeValidationHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopHealingRecipeValidationHandler.class)
                .orElseGet(() -> registerTroopHealingRecipeValidationHandler(new TroopHealingRecipeValidationHandler()));
    }

    default TroopHealingRepository getTroopHealingRepository() {
        return DependencyLoaderAccess.findInstance(TroopHealingRepository.class);
    }

    default TroopHealingStartHandler getTroopHealingStartHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopHealingStartHandler.class)
                .orElseGet(() -> registerTroopHealingStartHandler(new TroopHealingStartHandler()));
    }

    default TroopRepository getTroopRepository() {
        return DependencyLoaderAccess.findInstance(TroopRepository.class);
    }

    default TroopWoundAssignmentHandler getTroopWoundAssignmentHandler() {
        return DependencyLoaderAccess.findOptionalInstance(TroopWoundAssignmentHandler.class)
                .orElseGet(() -> registerTroopWoundAssignmentHandler(new TroopWoundAssignmentHandler()));
    }

    default DomainEventPublisher getDomainEventPublisher() {
        return DependencyLoaderAccess.findInstance(DomainEventPublisher.class);
    }

    default void registerGlobalAssetRepository(GlobalAssetRepository globalAssetRepository) {
        DependencyLoaderAccess.registerInstance(GlobalAssetRepository.class, globalAssetRepository);
    }

    default GlobalAssetResolutionHandler registerGlobalAssetResolutionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        DependencyLoaderAccess.registerInstance(GlobalAssetResolutionHandler.class, globalAssetResolutionHandler);
        return globalAssetResolutionHandler;
    }

    default HealingFacilityDefinitionRegistry registerHealingFacilityDefinitionRegistry(HealingFacilityDefinitionRegistry facilityDefinitionRegistry) {
        DependencyLoaderAccess.registerInstance(HealingFacilityDefinitionRegistry.class, facilityDefinitionRegistry);
        return facilityDefinitionRegistry;
    }

    default HealingFacilityModifierCalculationHandler registerHealingFacilityModifierCalculationHandler(HealingFacilityModifierCalculationHandler facilityModifierCalculationHandler) {
        DependencyLoaderAccess.registerInstance(HealingFacilityModifierCalculationHandler.class, facilityModifierCalculationHandler);
        return facilityModifierCalculationHandler;
    }

    default void registerHealingInventoryRepository(HealingInventoryRepository healingInventoryRepository) {
        DependencyLoaderAccess.registerInstance(HealingInventoryRepository.class, healingInventoryRepository);
    }

    default HealingItemCraftingHandler registerHealingItemCraftingHandler(HealingItemCraftingHandler healingItemCraftingHandler) {
        DependencyLoaderAccess.registerInstance(HealingItemCraftingHandler.class, healingItemCraftingHandler);
        return healingItemCraftingHandler;
    }

    default HealingResourceCostCalculationHandler registerHealingResourceCostCalculationHandler(HealingResourceCostCalculationHandler resourceCostCalculationHandler) {
        DependencyLoaderAccess.registerInstance(HealingResourceCostCalculationHandler.class, resourceCostCalculationHandler);
        return resourceCostCalculationHandler;
    }

    default MedicalItemCraftingHandler registerMedicalItemCraftingHandler(MedicalItemCraftingHandler medicalItemCraftingHandler) {
        DependencyLoaderAccess.registerInstance(MedicalItemCraftingHandler.class, medicalItemCraftingHandler);
        return medicalItemCraftingHandler;
    }

    default TroopHealingCompletionHandler registerTroopHealingCompletionHandler(TroopHealingCompletionHandler completionHandler) {
        DependencyLoaderAccess.registerInstance(TroopHealingCompletionHandler.class, completionHandler);
        return completionHandler;
    }

    default TroopHealingProgressTickHandler registerTroopHealingProgressTickHandler(TroopHealingProgressTickHandler progressTickHandler) {
        DependencyLoaderAccess.registerInstance(TroopHealingProgressTickHandler.class, progressTickHandler);
        return progressTickHandler;
    }

    default TroopHealingProjectionHandler registerTroopHealingProjectionHandler(TroopHealingProjectionHandler projectionHandler) {
        DependencyLoaderAccess.registerInstance(TroopHealingProjectionHandler.class, projectionHandler);
        return projectionHandler;
    }

    default TroopHealingRecipeSelectionHandler registerTroopHealingRecipeSelectionHandler(TroopHealingRecipeSelectionHandler recipeSelectionHandler) {
        DependencyLoaderAccess.registerInstance(TroopHealingRecipeSelectionHandler.class, recipeSelectionHandler);
        return recipeSelectionHandler;
    }

    default TroopHealingRecipeValidationHandler registerTroopHealingRecipeValidationHandler(TroopHealingRecipeValidationHandler recipeValidationHandler) {
        DependencyLoaderAccess.registerInstance(TroopHealingRecipeValidationHandler.class, recipeValidationHandler);
        return recipeValidationHandler;
    }

    default void registerTroopHealingRepository(TroopHealingRepository troopHealingRepository) {
        DependencyLoaderAccess.registerInstance(TroopHealingRepository.class, troopHealingRepository);
    }

    default TroopHealingStartHandler registerTroopHealingStartHandler(TroopHealingStartHandler healingStartHandler) {
        DependencyLoaderAccess.registerInstance(TroopHealingStartHandler.class, healingStartHandler);
        return healingStartHandler;
    }

    default void registerTroopRepository(TroopRepository troopRepository) {
        DependencyLoaderAccess.registerInstance(TroopRepository.class, troopRepository);
    }

    default TroopWoundAssignmentHandler registerTroopWoundAssignmentHandler(TroopWoundAssignmentHandler woundAssignmentHandler) {
        DependencyLoaderAccess.registerInstance(TroopWoundAssignmentHandler.class, woundAssignmentHandler);
        return woundAssignmentHandler;
    }

    default void registerDomainEventPublisher(DomainEventPublisher domainEventPublisher) {
        DependencyLoaderAccess.registerInstance(DomainEventPublisher.class, domainEventPublisher);
    }
}
