package org.tavall.control.healing;

import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.asset.InMemoryGlobalAssetRepository;
import org.tavall.control.common.GamePlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HealingResourceDefinitionIntegrationTest {
    @Test
    void fiveGemsHealingItemsCraftingResourcesAndFacilitiesRegisterAsGlobalAssets() {
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        HealingFacilityDefinitionRegistry facilityDefinitionRegistry = new HealingFacilityDefinitionRegistry();
        new HealingGlobalAssetBootstrapHandler(assetRepository, facilityDefinitionRegistry)
                .registerHealingGlobalAssets(Instant.parse("2026-04-30T14:00:00Z"));

        assertEquals(GemDomain.PROTECTION, GemType.PEARL.domain());
        assertEquals(GemDomain.MAGIC, GemType.AMETHYST.domain());
        assertEquals(GemDomain.ECONOMY, GemType.PERIDOT.domain());
        assertEquals(GemDomain.COMBAT, GemType.RUBY.domain());
        assertEquals(GemDomain.INTELLIGENCE, GemType.SAPPHIRE.domain());
        assertEquals("resource.gem.pearl", GemType.PEARL.globalAssetId().value());
        assertEquals("resource.gem.amethyst", GemType.AMETHYST.globalAssetId().value());

        for (GemType gemType : GemType.values()) {
            assertTrue(assetRepository.findGlobalAsset(gemType.globalAssetId()).isPresent());
        }
        for (HealingItemType healingItemType : HealingItemType.values()) {
            assertTrue(assetRepository.findGlobalAsset(healingItemType.globalAssetId()).isPresent());
        }
        for (MedicalCraftingResource resource : MedicalCraftingResource.values()) {
            assertTrue(assetRepository.findGlobalAsset(resource.globalAssetId()).isPresent());
        }
        assertEquals(30, facilityDefinitionRegistry.definitions().size());
        assertTrue(assetRepository.findGlobalAsset(facilityDefinitionRegistry.definitionForLevel(30).orElseThrow().globalAssetId()).isPresent());
    }

    @Test
    void resourcesResolveForMinecraftRobloxAndDiscordWithFallbackWhenPlatformArtIsMissing() {
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        new HealingGlobalAssetBootstrapHandler(assetRepository, new HealingFacilityDefinitionRegistry())
                .registerHealingGlobalAssets(Instant.parse("2026-04-30T14:05:00Z"));
        GlobalAssetResolutionHandler resolutionHandler = new GlobalAssetResolutionHandler(assetRepository);

        for (GamePlatform platform : new GamePlatform[]{GamePlatform.MINECRAFT, GamePlatform.ROBLOX, GamePlatform.DISCORD}) {
            assertTrue(resolutionHandler.resolveGlobalAssetForPlatform(HealingItemType.FIELD_RATIONS.globalAssetId(), platform).fallbackAssetKey().contains(HealingItemType.FIELD_RATIONS.globalAssetId().value()));
            assertTrue(resolutionHandler.resolveGlobalAssetForPlatform(GemType.AMETHYST.globalAssetId(), platform).fallbackAssetKey().contains(GemType.AMETHYST.globalAssetId().value()));
        }
    }
}
