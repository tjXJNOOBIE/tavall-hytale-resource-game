package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.asset.GlobalAsset;
import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.asset.GlobalAssetRepository;
import com.tavall.resourcegame.middleware.asset.GlobalAssetType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class HealingGlobalAssetBootstrapHandler implements IHealingDomain {
    public HealingGlobalAssetBootstrapHandler() {
    }

    public HealingGlobalAssetBootstrapHandler(
            GlobalAssetRepository globalAssetRepository,
            HealingFacilityDefinitionRegistry facilityDefinitionRegistry
    ) {
        registerGlobalAssetRepository(globalAssetRepository);
        registerHealingFacilityDefinitionRegistry(facilityDefinitionRegistry);
    }

    public void registerHealingGlobalAssets(Instant now) {
        for (GemType gemType : GemType.values()) {
            register(gemType.globalAssetId(), GlobalAssetType.RESOURCE, displayName(gemType.name()), Optional.of(gemType.domain().name() + " gem domain"), now, Map.of("gemDomain", gemType.domain().name()));
        }
        for (HealingItemType healingItemType : HealingItemType.values()) {
            register(healingItemType.globalAssetId(), GlobalAssetType.ITEM, displayName(healingItemType.name()), Optional.of("Crafted older-era healing item"), now, Map.of("healingItem", healingItemType.name()));
        }
        for (MedicalCraftingResource resource : MedicalCraftingResource.values()) {
            register(resource.globalAssetId(), GlobalAssetType.RESOURCE, displayName(resource.name()), Optional.of("Medical crafting resource from " + resource.nodeFamily().name() + " nodes"), now, Map.of("nodeFamily", resource.nodeFamily().name()));
        }
        for (HealingFacilityLevelDefinition definition : getHealingFacilityDefinitionRegistry().definitions()) {
            register(definition.globalAssetId(), GlobalAssetType.BUILDING, definition.facilityName() + " " + definition.buildingLevel(), Optional.of("Healing facility level " + definition.buildingLevel()), now, Map.of("facilityType", definition.facilityType().name()));
        }
    }

    private void register(
            GlobalAssetId globalAssetId,
            GlobalAssetType assetType,
            String displayName,
            Optional<String> description,
            Instant now,
            Map<String, String> metadata
    ) {
        if (getGlobalAssetRepository().findGlobalAsset(globalAssetId).isEmpty()) {
            getGlobalAssetRepository().saveGlobalAsset(new GlobalAsset(globalAssetId, assetType, displayName, description, now, metadata));
        }
    }

    private String displayName(String enumName) {
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }
}
