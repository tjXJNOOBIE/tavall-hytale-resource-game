package org.tavall.control.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.castle.ICastleBuildingService;
import org.tavall.control.ui.IUiActionService;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleBuildingSummary;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FarmsteadMenuPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/farmstead-menu.html";

    public FarmsteadMenuPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionService actionService,
            ICastleBuildingService buildingService
    ) {
        super(player, context, state, actionService, PAGE_DOCUMENT, templateData(player, context, state, buildingService), bindings());
    }

    private static Map<String, ?> templateData(Player player, UiNavigationContext context, PlayerGameState state, ICastleBuildingService buildingService) {
        Optional<CastleBuildingData> farmstead = buildingService.resolveBuilding(state, BuildingType.FARMSTEAD.shortKey());
        return Map.ofEntries(
                Map.entry("FarmsteadTitle", "Farmstead"),
                Map.entry("FarmsteadStatus", farmstead.map(building -> status(player, state, buildingService, building)).orElse("No Farmstead has been placed yet.")),
                Map.entry("FarmsteadFeedback", context.feedbackMessage().isBlank() ? "Choose a Farmstead action." : context.feedbackMessage()),
                Map.entry("CropsLabel", "Crops"),
                Map.entry("StorageLabel", "Storage"),
                Map.entry("WorkersLabel", "Workers"),
                Map.entry("UpgradeLabel", "Upgrade"),
                Map.entry("CloseLabel", "Close")
        );
    }

    private static List<HyUiActionBinding> bindings() {
        return List.of(
                HyUiActionBinding.action("#CropsButton", UiActions.OPEN_RESOURCES),
                HyUiActionBinding.action("#StorageButton", UiActions.OPEN_RESOURCES),
                HyUiActionBinding.action("#WorkersButton", UiActions.OPEN_CITIZENS),
                HyUiActionBinding.action("#UpgradeButton", UiActions.OPEN_FARMSTEAD_UPGRADE),
                HyUiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }

    private static String status(Player player, PlayerGameState state, ICastleBuildingService buildingService, CastleBuildingData building) {
        CastleBuildingSummary summary = buildingService.summary(player.getUuid(), state, building, Instant.now());
        if (summary.isUnderConstruction()) {
            return "Level " + summary.completedLevel() + " -> " + summary.displayLevel()
                    + " | " + summary.constructionStage().name().toLowerCase()
                    + " | " + (int) Math.round(summary.progressRatio() * 100.0D) + "%"
                    + " | " + summary.remainingSeconds() + "s left";
        }
        if (summary.nextUpgradeProfile() == null) {
            return "Level " + summary.completedLevel() + " | Max level reached";
        }
        return "Level " + summary.completedLevel() + " | Ready to upgrade";
    }
}

