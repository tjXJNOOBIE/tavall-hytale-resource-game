package com.tavall.resourcegame.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tavall.resourcegame.dependency.interfaces.ICastleBuildingService;
import com.tavall.resourcegame.dependency.interfaces.IUiActionService;
import com.tavall.resourcegame.domain.BuildingType;
import com.tavall.resourcegame.domain.CastleBuildingData;
import com.tavall.resourcegame.domain.CastleBuildingSummary;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.domain.UiNavigationContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Overview page for castle and interior building progression.
 */
public final class CastleBuildingsPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-buildings.html";

    public CastleBuildingsPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionService actionService,
            ICastleBuildingService buildingService
    ) {
        super(player, context, state, actionService, PAGE_DOCUMENT, templateData(player, context, state, buildingService), bindings());
    }

    private static Map<String, ?> templateData(Player player, UiNavigationContext context, PlayerGameState state, ICastleBuildingService buildingService) {
        return Map.ofEntries(
                Map.entry("FarmsteadStatus", status(player, state, buildingService, BuildingType.FARMSTEAD)),
                Map.entry("LumberMillStatus", status(player, state, buildingService, BuildingType.LUMBER_MILL)),
                Map.entry("IronWorksStatus", status(player, state, buildingService, BuildingType.IRON_WORKS)),
                Map.entry("BarracksStatus", status(player, state, buildingService, BuildingType.BARRACKS)),
                Map.entry("WorkshopStatus", status(player, state, buildingService, BuildingType.WORKSHOP)),
                Map.entry(
                        "FooterStatus",
                        context.feedbackMessage().isBlank()
                        ? "Place buildings with /kd buildings place <type>, then interact with them in-world to open detail and start upgrades."
                        : context.feedbackMessage()
                )
        );
    }

    private static List<HyUiActionBinding> bindings() {
        return List.of(
                HyUiActionBinding.action("#StageFarmsteadButton", UiActions.BUILDING_STAGE, BuildingType.FARMSTEAD.shortKey(), UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageLumberMillButton", UiActions.BUILDING_STAGE, BuildingType.LUMBER_MILL.shortKey(), UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageIronWorksButton", UiActions.BUILDING_STAGE, BuildingType.IRON_WORKS.shortKey(), UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageBarracksButton", UiActions.BUILDING_STAGE, BuildingType.BARRACKS.shortKey(), UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#StageWorkshopButton", UiActions.BUILDING_STAGE, BuildingType.WORKSHOP.shortKey(), UiPageType.CASTLE_BUILDINGS),
                HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN)
        );
    }

    private static String status(Player player, PlayerGameState state, ICastleBuildingService buildingService, BuildingType buildingType) {
        Optional<CastleBuildingData> building = buildingService.resolveBuilding(state, buildingType.shortKey());
        if (building.isEmpty()) {
            return "Missing | Place in " + buildingType.areaType().displayName() + " | " + buildingType.description();
        }
        CastleBuildingSummary summary = buildingService.summary(player.getUuid(), state, building.get(), Instant.now());
        if (summary.isUnderConstruction()) {
            return "L" + summary.completedLevel()
                    + " -> L" + summary.displayLevel()
                    + " | " + summary.constructionStage().name().toLowerCase()
                    + " " + (int) Math.round(summary.progressRatio() * 100.0D) + "%"
                    + " | " + summary.remainingSeconds() + "s";
        }
        return "L" + summary.completedLevel()
                + " | " + effectSummary(summary)
                + (summary.nextUpgradeProfile() == null ? " | Maxed" : " | Ready for next upgrade");
    }

    private static String effectSummary(CastleBuildingSummary summary) {
        if (summary.foodPerTickBonus() > 0 || summary.woodPerTickBonus() > 0 || summary.ironPerTickBonus() > 0) {
            return "+" + summary.foodPerTickBonus() + "F +" + summary.woodPerTickBonus() + "W +" + summary.ironPerTickBonus() + "I / tick";
        }
        if (summary.constructionSpeedBonus() > 0.0D) {
            return "Build speed +" + (int) Math.round(summary.constructionSpeedBonus() * 100.0D) + "%";
        }
        return "Promotion discount -" + summary.promotionDiscount().foodCost()
                + "/" + summary.promotionDiscount().woodCost()
                + "/" + summary.promotionDiscount().ironCost();
    }
}
