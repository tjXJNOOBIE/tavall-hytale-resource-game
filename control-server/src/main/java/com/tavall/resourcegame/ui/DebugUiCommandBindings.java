package com.tavall.resourcegame.ui;

import java.util.List;

/**
 * HyUI bindings for debug command pages.
 */
public final class DebugUiCommandBindings {
    private DebugUiCommandBindings() {
    }

    public static List<HyUiActionBinding> navigator() {
        return List.of(
                HyUiActionBinding.action("#CastleMainButton", UiActions.OPEN_CASTLE_MAIN),
                HyUiActionBinding.action("#CastleInfoButton", UiActions.OPEN_CASTLE_INFO),
                HyUiActionBinding.action("#CitizensButton", UiActions.OPEN_CITIZENS),
                HyUiActionBinding.action("#TroopsButton", UiActions.OPEN_TROOPS),
                HyUiActionBinding.action("#ResourcesButton", UiActions.OPEN_RESOURCES),
                HyUiActionBinding.action("#UpgradesButton", UiActions.OPEN_UPGRADES),
                HyUiActionBinding.action("#InteriorButton", UiActions.ENTER_INTERIOR),
                HyUiActionBinding.action("#DebugPlacementButton", UiActions.OPEN_DEBUG_PLACEMENT),
                HyUiActionBinding.action("#DebugInteriorButton", UiActions.OPEN_DEBUG_INTERIOR),
                HyUiActionBinding.action("#DebugBuildingsButton", UiActions.OPEN_DEBUG_BUILDINGS),
                HyUiActionBinding.action("#DebugWorldButton", UiActions.OPEN_DEBUG_WORLD),
                HyUiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }

    public static List<HyUiActionBinding> placement() {
        return List.of(
                HyUiActionBinding.action("#PlaceCastleButton", UiActions.PLACEMENT_ARM_CASTLE),
                HyUiActionBinding.action("#PlaceFoodNodeButton", UiActions.PLACEMENT_ARM_NODE, "food"),
                HyUiActionBinding.action("#PlaceWoodNodeButton", UiActions.PLACEMENT_ARM_NODE, "wood"),
                HyUiActionBinding.action("#PlaceIronNodeButton", UiActions.PLACEMENT_ARM_NODE, "iron"),
                HyUiActionBinding.action("#ConfirmPlacementButton", UiActions.PLACEMENT_CONFIRM),
                HyUiActionBinding.action("#CancelPlacementButton", UiActions.PLACEMENT_CANCEL),
                HyUiActionBinding.action("#MoveNegXButton", UiActions.PLACEMENT_MOVE, "-1,0,0"),
                HyUiActionBinding.action("#MovePosXButton", UiActions.PLACEMENT_MOVE, "1,0,0"),
                HyUiActionBinding.action("#MoveNegZButton", UiActions.PLACEMENT_MOVE, "0,0,-1"),
                HyUiActionBinding.action("#MovePosZButton", UiActions.PLACEMENT_MOVE, "0,0,1"),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }

    public static List<HyUiActionBinding> interior() {
        return List.of(
                HyUiActionBinding.action("#InteriorGenerateButton", UiActions.DEBUG_INTERIOR_GENERATE),
                HyUiActionBinding.action("#InteriorRebuildButton", UiActions.DEBUG_INTERIOR_REBUILD),
                HyUiActionBinding.action("#InteriorDeleteButton", UiActions.DEBUG_INTERIOR_DELETE),
                HyUiActionBinding.action("#InteriorMoveButton", UiActions.DEBUG_INTERIOR_MOVE),
                HyUiActionBinding.action("#InteriorExitButton", UiActions.DEBUG_INTERIOR_EXIT),
                HyUiActionBinding.action("#SceneRefreshButton", UiActions.DEBUG_SCENE_REFRESH, "", UiPageType.DEBUG_INTERIOR),
                HyUiActionBinding.action("#TutorialResetButton", UiActions.DEBUG_TUTORIAL_RESET, "", UiPageType.DEBUG_INTERIOR),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }

    public static List<HyUiActionBinding> buildings() {
        return List.of(
                HyUiActionBinding.action("#BuildingsListButton", UiActions.DEBUG_BUILDINGS_LIST),
                HyUiActionBinding.action("#PlaceFarmsteadButton", UiActions.BUILDING_PLACE, "farmstead", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#PlaceLumberMillButton", UiActions.BUILDING_PLACE, "lumber_mill", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#PlaceIronWorksButton", UiActions.BUILDING_PLACE, "iron_works", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#PlaceBarracksButton", UiActions.BUILDING_PLACE, "barracks", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#PlaceWorkshopButton", UiActions.BUILDING_PLACE, "workshop", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#StageFarmsteadButton", UiActions.BUILDING_STAGE, "farmstead", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#StageLumberMillButton", UiActions.BUILDING_STAGE, "lumber_mill", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#StageIronWorksButton", UiActions.BUILDING_STAGE, "iron_works", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#StageBarracksButton", UiActions.BUILDING_STAGE, "barracks", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#StageWorkshopButton", UiActions.BUILDING_STAGE, "workshop", UiPageType.DEBUG_BUILDINGS),
                HyUiActionBinding.action("#FocusButton", UiActions.DEBUG_FOCUS),
                HyUiActionBinding.action("#InteractButton", UiActions.DEBUG_INTERACT),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }

    public static List<HyUiActionBinding> world() {
        return List.of(
                HyUiActionBinding.action("#NodesListButton", UiActions.DEBUG_NODES_LIST),
                HyUiActionBinding.action("#NodesClearButton", UiActions.DEBUG_NODES_CLEAR),
                HyUiActionBinding.action("#HologramTestButton", UiActions.DEBUG_HOLOGRAM_TEST),
                HyUiActionBinding.action("#SpawnFarmerButton", UiActions.DEBUG_ENTITY_SPAWN, "farmer"),
                HyUiActionBinding.action("#SpawnMinerButton", UiActions.DEBUG_ENTITY_SPAWN, "miner"),
                HyUiActionBinding.action("#SpawnSoldierButton", UiActions.DEBUG_ENTITY_SPAWN, "soldier"),
                HyUiActionBinding.action("#ClearEntitiesButton", UiActions.DEBUG_ENTITY_CLEAR),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_DEBUG)
        );
    }
}
