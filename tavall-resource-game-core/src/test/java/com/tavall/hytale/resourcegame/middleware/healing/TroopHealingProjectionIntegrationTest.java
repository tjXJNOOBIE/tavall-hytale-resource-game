package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetResolutionHandler;
import com.tavall.hytale.resourcegame.middleware.asset.InMemoryGlobalAssetRepository;
import com.tavall.hytale.resourcegame.middleware.asset.PlatformAssetVersionRegistrationHandler;
import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.identity.InMemoryIdentityRepository;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformAccountLinkHandler;
import com.tavall.hytale.resourcegame.middleware.identity.PlatformLinkChallengeCreated;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.platform.DiscordPlatformAccountBindingHandler;
import com.tavall.hytale.resourcegame.middleware.platform.RobloxPlatformAccountBindingHandler;
import com.tavall.hytale.resourcegame.middleware.projection.PlatformInteractionType;
import com.tavall.hytale.resourcegame.middleware.security.Sha256TokenHasher;
import com.tavall.hytale.resourcegame.middleware.troop.InMemoryTroopRepository;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TroopHealingProjectionIntegrationTest {
    @Test
    void minecraftAndHytaleHealingProjectionsReadCanonicalMiddlewareState() {
        ProjectionFixture fixture = projectionFixture();
        TroopHealingProjection minecraftProjection = new MinecraftTroopHealingProjectionHandler(fixture.projectionHandler())
                .projectTroopHealingForMinecraftClient(fixture.troop(), fixture.wound(), fixture.rationOnlyInventory(), fixture.facility());
        TroopHealingProjection hytaleProjection = new HytaleTroopHealingProjectionHandler(fixture.projectionHandler())
                .projectTroopHealingForHytaleClient(fixture.troop(), fixture.wound(), fixture.rationOnlyInventory(), fixture.facility());

        assertEquals(GamePlatform.MINECRAFT, minecraftProjection.platform());
        assertTrue(minecraftProjection.globalAssetIds().contains(HealingItemType.BANDAGE_KIT.globalAssetId()));
        assertEquals("minecraft:item/bandage_kit", minecraftProjection.platformAssetReferences().get(HealingItemType.BANDAGE_KIT.globalAssetId()));
        assertEquals(GamePlatform.HYTALE, hytaleProjection.platform());
        assertTrue(hytaleProjection.platformAssetReferences().get(GemType.PEARL.globalAssetId()).contains("hytale:item/pearl"));
        assertEquals("middleware-control-server", minecraftProjection.metadata().get("canonicalStateOwner"));
    }

    @Test
    void projectionKeepsFoodOnlyEnabledWhenProperTreatmentIsBlocked() {
        ProjectionFixture fixture = projectionFixture();
        TroopHealingProjection projection = new MinecraftTroopHealingProjectionHandler(fixture.projectionHandler())
                .projectTroopHealingForMinecraftClient(fixture.troop(), fixture.wound(), fixture.rationOnlyInventory(), fixture.facility());

        TroopHealingProjectionOption foodOnly = projection.healingOptions().stream()
                .filter(option -> option.healingMode() == HealingMode.FOOD_ONLY)
                .findFirst()
                .orElseThrow();
        TroopHealingProjectionOption properTreatment = projection.healingOptions().stream()
                .filter(option -> option.healingMode() == HealingMode.PROPER_TREATMENT)
                .findFirst()
                .orElseThrow();

        assertTrue(foodOnly.enabled());
        assertFalse(properTreatment.enabled());
        assertTrue(properTreatment.disabledReason().orElseThrow().contains("Missing resources"));
        assertTrue(properTreatment.missingResources().contains(HealingItemType.BANDAGE_KIT.globalAssetId()));
        assertTrue(properTreatment.requiredGemType().orElseThrow() == GemType.PEARL);
    }

    @Test
    void robloxAndDiscordProjectionTestsUseInMemoryBindingsAndFallbackAssets() {
        ProjectionFixture fixture = projectionFixture();
        UniversalPlayerId playerId = UniversalPlayerId.random();
        InMemoryIdentityRepository identityRepository = new InMemoryIdentityRepository();
        PlatformAccountLinkHandler linkHandler = new PlatformAccountLinkHandler(identityRepository, identityRepository, new Sha256TokenHasher("projection-pepper"), new SecureRandom(new byte[]{4, 4, 4}));
        Instant now = Instant.parse("2026-04-30T15:10:00Z");
        RobloxPlatformAccountBindingHandler robloxBindingHandler = new RobloxPlatformAccountBindingHandler(linkHandler);
        PlatformLinkChallengeCreated robloxChallenge = robloxBindingHandler.createRobloxLinkChallenge(playerId, Duration.ofMinutes(5), now);
        assertEquals(GamePlatform.ROBLOX, robloxBindingHandler.bindRobloxAccount(robloxChallenge.challenge().challengeId(), robloxChallenge.shortCode(), "123456", "RobloxHealer", now.plusSeconds(1)).platform());
        DiscordPlatformAccountBindingHandler discordBindingHandler = new DiscordPlatformAccountBindingHandler(linkHandler);
        PlatformLinkChallengeCreated discordChallenge = discordBindingHandler.createDiscordLinkChallenge(playerId, Duration.ofMinutes(5), now.plusSeconds(2));
        assertEquals(GamePlatform.DISCORD, discordBindingHandler.bindDiscordAccount(discordChallenge.challenge().challengeId(), discordChallenge.shortCode(), "discord-123456", "DiscordHealer", now.plusSeconds(3)).platform());

        TroopHealingProjection robloxProjection = new RobloxTroopHealingProjectionHandler(fixture.projectionHandler())
                .projectTroopHealingForRobloxClient(fixture.troop(), fixture.wound(), fixture.rationOnlyInventory(), fixture.facility());
        TroopHealingProjection discordProjection = new DiscordTroopHealingProjectionHandler(fixture.projectionHandler())
                .projectTroopHealingForDiscordClient(fixture.troop(), fixture.wound(), fixture.rationOnlyInventory(), fixture.facility());

        assertEquals(PlatformInteractionType.ROBLOX_REMOTE_EVENT, robloxProjection.healingOptions().getFirst().interactionType());
        assertTrue(robloxProjection.platformAssetReferences().get(HealingItemType.BANDAGE_KIT.globalAssetId()).startsWith("fallback:roblox:"));
        assertEquals(PlatformInteractionType.DISCORD_BUTTON, discordProjection.healingOptions().getFirst().interactionType());
        assertTrue(discordProjection.interactionActions().stream().anyMatch(action -> action.interactionType() == PlatformInteractionType.DISCORD_SELECT_MENU));
        assertTrue(discordProjection.interactionActions().stream().anyMatch(action -> action.interactionType() == PlatformInteractionType.DISCORD_SLASH_COMMAND));
    }

    private ProjectionFixture projectionFixture() {
        Instant now = Instant.parse("2026-04-30T15:00:00Z");
        InMemoryGlobalAssetRepository assetRepository = new InMemoryGlobalAssetRepository();
        new HealingGlobalAssetBootstrapHandler(assetRepository, new HealingFacilityDefinitionRegistry()).registerHealingGlobalAssets(now);
        PlatformAssetVersionRegistrationHandler assetVersionHandler = new PlatformAssetVersionRegistrationHandler(assetRepository);
        assetVersionHandler.registerPlatformAssetVersion(HealingItemType.BANDAGE_KIT.globalAssetId(), GamePlatform.MINECRAFT, "minecraft:item/bandage_kit", 1, Optional.empty(), now);
        assetVersionHandler.registerPlatformAssetVersion(GemType.PEARL.globalAssetId(), GamePlatform.HYTALE, "hytale:item/pearl", 1, Optional.empty(), now);
        HealingFacilityModifierCalculationHandler modifierCalculationHandler = new HealingFacilityModifierCalculationHandler();
        TroopHealingProjectionHandler projectionHandler = new TroopHealingProjectionHandler(
                new TroopHealingRecipeSelectionHandler(),
                new TroopHealingRecipeValidationHandler(new HealingResourceCostCalculationHandler(modifierCalculationHandler), modifierCalculationHandler),
                new GlobalAssetResolutionHandler(assetRepository)
        );
        Troop troop = new TroopRegistrationHandler(new InMemoryTroopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 1, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
        TroopWound wound = TroopWound.active(troop.troopId(), WoundType.GENERAL_WOUND, WoundSeverity.MINOR, now);
        HealingInventory inventory = new HealingInventory(Map.of(HealingItemType.FIELD_RATIONS.globalAssetId(), 10));
        return new ProjectionFixture(projectionHandler, troop, wound, inventory, new HealingFacilityDefinitionRegistry().definitionForLevel(1));
    }
}
