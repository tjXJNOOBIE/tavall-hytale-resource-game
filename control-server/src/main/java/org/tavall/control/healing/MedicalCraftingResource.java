package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.node.MiddlewareResourceType;

public enum MedicalCraftingResource {
    FOOD_GRAIN("resource.food.grain", MiddlewareResourceType.FOOD),
    CLEAN_WATER("resource.natural.clean_water", MiddlewareResourceType.WATER),
    TIMBER("resource.natural.timber", MiddlewareResourceType.WOOD),
    WILLOW_BARK("resource.natural.willow_bark", MiddlewareResourceType.WOOD),
    HERBS("resource.medical.herbs", MiddlewareResourceType.HERB),
    HONEY("resource.medical.honey", MiddlewareResourceType.FOOD),
    HEALING_SAP("resource.medical.healing_sap", MiddlewareResourceType.WOOD),
    ANTIDOTE_ROOT("resource.medical.antidote_root", MiddlewareResourceType.HERB),
    CRYSTAL_MOSS("resource.medical.crystal_moss", MiddlewareResourceType.HERB),
    LINEN("resource.fiber.linen", MiddlewareResourceType.HERB),
    IRON("resource.metal.iron", MiddlewareResourceType.IRON);

    private final GlobalAssetId globalAssetId;
    private final MiddlewareResourceType nodeFamily;

    MedicalCraftingResource(String globalAssetId, MiddlewareResourceType nodeFamily) {
        this.globalAssetId = new GlobalAssetId(globalAssetId);
        this.nodeFamily = nodeFamily;
    }

    public GlobalAssetId globalAssetId() {
        return globalAssetId;
    }

    public MiddlewareResourceType nodeFamily() {
        return nodeFamily;
    }
}
