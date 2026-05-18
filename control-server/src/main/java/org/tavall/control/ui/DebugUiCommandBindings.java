package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import java.util.List;

/**
 * HyUI bindings for debug command pages.
 */
public final class DebugUiCommandBindings {
    private DebugUiCommandBindings() {
    }

    public static List<UiActionBinding> navigator() {
        return List.of(
                UiActionBinding.action("#CastleMainButton", UiActions.OPEN_CASTLE_MAIN),
                UiActionBinding.action("#CastleInfoButton", UiActions.OPEN_CASTLE_INFO),
                UiActionBinding.action("#CitizensButton", UiActions.OPEN_CITIZENS),
                UiActionBinding.action("#TroopsButton", UiActions.OPEN_TROOPS),
                UiActionBinding.action("#ResourcesButton", UiActions.OPEN_RESOURCES),
                UiActionBinding.action("#UpgradesButton", UiActions.OPEN_UPGRADES),
                UiActionBinding.action("#InteriorButton", UiActions.ENTER_INTERIOR),
                UiActionBinding.action("#DebugPlacementButton", UiActions.OPEN_DEBUG_PLACEMENT),
                UiActionBinding.action("#DebugInteriorButton", UiActions.OPEN_DEBUG_INTERIOR),
                UiActionBinding.action("#DebugBuildingsButton", UiActions.OPEN_DEBUG_BUILDINGS),
                UiActionBinding.action("#DebugWorldButton", UiActions.OPEN_DEBUG_WORLD),
                UiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }

    public static List<UiActionBinding> placement() {
        return List.of(
                UiActionBinding.action("#PlaceCastleButton", UiActions.PLACEMENT_ARM_CASTLE),
                UiActionBinding.action("#PlaceFoodNodeButton", UiActions.PLACEMENT_ARM_NODE, "food"),
                UiActionBinding.action("#PlaceWoodNodeButton", UiActions.PLACEMENT_ARM_NODE, "wood"),
                UiActionBinding.action("#PlaceIronNodeButton", UiActions.PLACEMENT_ARM_NODE, "iron"),
                UiActionBinding.action("#ConfirmPlacementButton", UiActions.PLACEMENT_CONFIRM),
                UiActionBinding.action("#CancelPlacementButton", UiActions.PLACEMENT_CANCEL),
                UiActionBinding.action("#MoveNegXButton", UiActions.PLACEMENT_MOVE, "-1,0,0"),
                UiActionBinding.action("#MovePosXButton", UiActions.PLACEMENT_MOVE, "1,0,0"),
                UiActionBinding.action("#MoveNegZButton", UiActions.PLACEMENT_MOVE, "0,0,-1"),
                UiActionBinding.action("#MovePosZButton", UiActions.PLACEMENT_MOVE, "0,0,1"),
                UiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }

    public static List<UiActionBinding> interior() {
        return List.of(
                UiActionBinding.action("#InteriorGenerateButton", UiActions.DEBUG_INTERIOR_GENERATE),
                UiActionBinding.action("#InteriorRebuildButton", UiActions.DEBUG_INTERIOR_REBUILD),
                UiActionBinding.action("#InteriorDeleteButton", UiActions.DEBUG_INTERIOR_DELETE),
                UiActionBinding.action("#InteriorMoveButton", UiActions.DEBUG_INTERIOR_MOVE),
                UiActionBinding.action("#InteriorExitButton", UiActions.DEBUG_INTERIOR_EXIT),
                UiActionBinding.action("#SceneRefreshButton", UiActions.DEBUG_SCENE_REFRESH, "", UiPageType.DEBUG_INTERIOR),
                UiActionBinding.action("#TutorialResetButton", UiActions.DEBUG_TUTORIAL_RESET, "", UiPageType.DEBUG_INTERIOR),
                UiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }

    public static List<UiActionBinding> buildings() {
        return List.of(
                UiActionBinding.action("#BuildingsListButton", UiActions.DEBUG_BUILDINGS_LIST),
                UiActionBinding.action("#PlaceFarmsteadButton", UiActions.BUILDING_PLACE, "farmstead", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#PlaceLumberMillButton", UiActions.BUILDING_PLACE, "lumber_mill", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#PlaceIronWorksButton", UiActions.BUILDING_PLACE, "iron_works", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#PlaceBarracksButton", UiActions.BUILDING_PLACE, "barracks", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#PlaceWorkshopButton", UiActions.BUILDING_PLACE, "workshop", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#StageFarmsteadButton", UiActions.BUILDING_STAGE, "farmstead", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#StageLumberMillButton", UiActions.BUILDING_STAGE, "lumber_mill", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#StageIronWorksButton", UiActions.BUILDING_STAGE, "iron_works", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#StageBarracksButton", UiActions.BUILDING_STAGE, "barracks", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#StageWorkshopButton", UiActions.BUILDING_STAGE, "workshop", UiPageType.DEBUG_BUILDINGS),
                UiActionBinding.action("#FocusButton", UiActions.DEBUG_FOCUS),
                UiActionBinding.action("#InteractButton", UiActions.DEBUG_INTERACT),
                UiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }

    public static List<UiActionBinding> world() {
        return List.of(
                UiActionBinding.action("#NodesListButton", UiActions.DEBUG_NODES_LIST),
                UiActionBinding.action("#NodesClearButton", UiActions.DEBUG_NODES_CLEAR),
                UiActionBinding.action("#HologramTestButton", UiActions.DEBUG_HOLOGRAM_TEST),
                UiActionBinding.action("#SpawnFarmerButton", UiActions.DEBUG_ENTITY_SPAWN, "farmer"),
                UiActionBinding.action("#SpawnMinerButton", UiActions.DEBUG_ENTITY_SPAWN, "miner"),
                UiActionBinding.action("#SpawnSoldierButton", UiActions.DEBUG_ENTITY_SPAWN, "soldier"),
                UiActionBinding.action("#ClearEntitiesButton", UiActions.DEBUG_ENTITY_CLEAR),
                UiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }
}
