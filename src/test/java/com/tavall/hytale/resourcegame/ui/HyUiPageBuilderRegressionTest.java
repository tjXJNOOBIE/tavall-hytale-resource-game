package com.tavall.hytale.resourcegame.ui;

import au.ellie.hyui.builders.UIElementBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HyUiPageBuilderRegressionTest {
    @Test
    void hyUiPagesBindEveryExpectedActionSelector() {
        assertPageBuilds("Pages/castle-main.html", castleMainBindings());
        assertPageBuilds("Pages/castle-info.html", castleInfoBindings());
        assertPageBuilds("Pages/castle-citizens.html", List.of(HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)));
        assertPageBuilds("Pages/castle-troops.html", List.of(HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)));
        assertPageBuilds("Pages/castle-resources.html", List.of(HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)));
        assertPageBuilds("Pages/castle-upgrades.html", castleUpgradeBindings());
        assertPageBuilds("Pages/castle-buildings.html", castleBuildingBindings());
        assertPageBuilds("Pages/farmstead-menu.html", farmsteadMenuBindings());
        assertPageBuilds("Pages/building-detail.html", buildingDetailBindings());
        assertPageBuilds("Pages/resource-node-detail.html", resourceNodeBindings());
        assertPageBuilds("Pages/interior-main.html", interiorBindings());
        assertPageBuilds("Pages/debug-navigator.html", DebugUiCommandBindings.navigator());
        assertPageBuilds("Pages/debug-placement.html", DebugUiCommandBindings.placement());
        assertPageBuilds("Pages/debug-interior.html", DebugUiCommandBindings.interior());
        assertPageBuilds("Pages/debug-buildings.html", DebugUiCommandBindings.buildings());
        assertPageBuilds("Pages/debug-world.html", DebugUiCommandBindings.world());
    }

    @Test
    void debugPagesKeepSmallEventBindingBatches() {
        assertTrue(DebugUiCommandBindings.navigator().size() <= 16);
        assertTrue(DebugUiCommandBindings.placement().size() <= 16);
        assertTrue(DebugUiCommandBindings.interior().size() <= 16);
        assertTrue(DebugUiCommandBindings.buildings().size() <= 16);
        assertTrue(DebugUiCommandBindings.world().size() <= 16);
    }

    private static void assertPageBuilds(String resourcePath, List<HyUiActionBinding> bindings) {
        HyUiPageDefinition definition = ResourceGameHyUiPageBuilder.build(
                resourcePath,
                Map.of(),
                bindings,
                (eventData, uiContext) -> {
                }
        );
        assertFalse(definition.topLevelElements().isEmpty(), () -> "No top-level HYUIML elements for " + resourcePath);
        assertNotNull(definition.templateHtml(), () -> "HyUI template HTML is missing for " + resourcePath);
        assertEventSelectorsUseActivatingControls(definition, bindings, resourcePath);
    }

    private static void assertEventSelectorsUseActivatingControls(
            HyUiPageDefinition definition,
            List<HyUiActionBinding> bindings,
            String resourcePath
    ) {
        try {
            Method selectorMethod = UIElementBuilder.class.getDeclaredMethod("getSelector");
            selectorMethod.setAccessible(true);
            for (HyUiActionBinding binding : bindings) {
                UIElementBuilder<?> matchingElement = definition.rootElementBuilder()
                        .getElements()
                        .stream()
                        .filter(element -> binding.elementId().equals(element.getId()))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Missing HyUI element for binding " + binding.elementId() + " in " + resourcePath));
                String selector = (String) selectorMethod.invoke(matchingElement);
                assertTrue(
                        selector.contains("#HyUIButton"),
                        () -> "HyUI action buttons should use raw image-backed Resource Game chrome: " + resourcePath + " " + binding.elementId() + " -> " + selector
                );
                assertFalse(UiActions.RUN_COMMAND.equals(binding.eventData().action()), () -> "UI clicks must route internal actions instead of commands: " + binding.elementId());
                String payload = binding.eventData().payload();
                assertFalse(payload != null && payload.trim().startsWith("/"), () -> "UI click payload should not be a command line: " + binding.elementId());
            }
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to inspect HyUI event selectors for " + resourcePath, exception);
        }
    }

    private static List<HyUiActionBinding> castleMainBindings() {
        return List.of(
                HyUiActionBinding.action("#EnterInteriorButton", UiActions.ENTER_INTERIOR),
                HyUiActionBinding.action("#CastleInfoButton", UiActions.OPEN_CASTLE_INFO),
                HyUiActionBinding.action("#CitizensButton", UiActions.OPEN_CITIZENS),
                HyUiActionBinding.action("#TroopsButton", UiActions.OPEN_TROOPS),
                HyUiActionBinding.action("#ResourcesButton", UiActions.OPEN_RESOURCES),
                HyUiActionBinding.action("#UpgradesButton", UiActions.OPEN_UPGRADES),
                HyUiActionBinding.action("#BuildingsButton", UiActions.OPEN_BUILDINGS),
                HyUiActionBinding.action("#AttackButton", UiActions.CASTLE_ATTACK_PLACEHOLDER),
                HyUiActionBinding.action("#FriendButton", UiActions.CASTLE_FRIEND_PLACEHOLDER),
                HyUiActionBinding.action("#GuildButton", UiActions.CASTLE_GUILD_PLACEHOLDER),
                HyUiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }

    private static List<HyUiActionBinding> castleInfoBindings() {
        return List.of(
                HyUiActionBinding.action("#AttackButton", UiActions.CASTLE_ATTACK_PLACEHOLDER),
                HyUiActionBinding.action("#FriendButton", UiActions.CASTLE_FRIEND_PLACEHOLDER),
                HyUiActionBinding.action("#GuildButton", UiActions.CASTLE_GUILD_PLACEHOLDER),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }

    private static List<HyUiActionBinding> castleUpgradeBindings() {
        return List.of(
                HyUiActionBinding.action("#PromoteButton", UiActions.PROMOTE),
                HyUiActionBinding.action("#DemoteButton", UiActions.DEMOTE),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }

    private static List<HyUiActionBinding> castleBuildingBindings() {
        return List.of(
                HyUiActionBinding.action("#StageFarmsteadButton", UiActions.BUILDING_STAGE, "farmstead", UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageLumberMillButton", UiActions.BUILDING_STAGE, "lumber_mill", UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageIronWorksButton", UiActions.BUILDING_STAGE, "iron_works", UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageBarracksButton", UiActions.BUILDING_STAGE, "barracks", UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageWorkshopButton", UiActions.BUILDING_STAGE, "workshop", UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }

    private static List<HyUiActionBinding> farmsteadMenuBindings() {
        return List.of(
                HyUiActionBinding.action("#CropsButton", UiActions.OPEN_RESOURCES),
                HyUiActionBinding.action("#StorageButton", UiActions.OPEN_RESOURCES),
                HyUiActionBinding.action("#WorkersButton", UiActions.OPEN_CITIZENS),
                HyUiActionBinding.action("#UpgradeButton", UiActions.OPEN_FARMSTEAD_UPGRADE),
                HyUiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }

    private static List<HyUiActionBinding> buildingDetailBindings() {
        return List.of(
                HyUiActionBinding.action("#StartUpgradeButton", UiActions.BUILDING_START_UPGRADE),
                HyUiActionBinding.action("#CancelUpgradeButton", UiActions.BUILDING_CANCEL_UPGRADE),
                HyUiActionBinding.action("#OverviewButton", UiActions.OPEN_BUILDINGS),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }

    private static List<HyUiActionBinding> resourceNodeBindings() {
        return List.of(
                HyUiActionBinding.action("#AssignOneButton", UiActions.NODE_ASSIGN_ONE),
                HyUiActionBinding.action("#AssignThreeButton", UiActions.NODE_ASSIGN_THREE),
                HyUiActionBinding.action("#AssignFiveButton", UiActions.NODE_ASSIGN_FIVE),
                HyUiActionBinding.action("#AssignAllButton", UiActions.NODE_ASSIGN_ALL),
                HyUiActionBinding.action("#RecallOneButton", UiActions.NODE_RECALL_ONE),
                HyUiActionBinding.action("#RecallAllButton", UiActions.NODE_RECALL_ALL),
                HyUiActionBinding.action("#PillageButton", UiActions.NODE_PILLAGE),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_RESOURCES)
        );
    }

    private static List<HyUiActionBinding> interiorBindings() {
        return List.of(
                HyUiActionBinding.action("#ExitInteriorButton", UiActions.EXIT_INTERIOR),
                HyUiActionBinding.action("#BuildingsButton", UiActions.OPEN_BUILDINGS),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }

}
