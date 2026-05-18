package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.ui.IUiActionHandler;
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
            IUiActionHandler actionHandler,
            ICastleBuildingHandler buildingHandler
    ) {
        super(player, context, state, actionHandler, PAGE_DOCUMENT, templateData(player, context, state, buildingHandler), bindings());
    }

    private static Map<String, ?> templateData(Player player, UiNavigationContext context, PlayerGameState state, ICastleBuildingHandler buildingHandler) {
        Optional<CastleBuildingData> farmstead = buildingHandler.resolveBuilding(state, BuildingType.FARMSTEAD.shortKey());
        return Map.ofEntries(
                Map.entry("FarmsteadTitle", "Farmstead"),
                Map.entry("FarmsteadStatus", farmstead.map(building -> status(player, state, buildingHandler, building)).orElse("No Farmstead has been placed yet.")),
                Map.entry("FarmsteadFeedback", context.feedbackMessage().isBlank() ? "Choose a Farmstead action." : context.feedbackMessage()),
                Map.entry("CropsLabel", "Crops"),
                Map.entry("StorageLabel", "Storage"),
                Map.entry("WorkersLabel", "Workers"),
                Map.entry("UpgradeLabel", "Upgrade"),
                Map.entry("CloseLabel", "Close")
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#CropsButton", UiActions.OPEN_RESOURCES),
                UiActionBinding.action("#StorageButton", UiActions.OPEN_RESOURCES),
                UiActionBinding.action("#WorkersButton", UiActions.OPEN_CITIZENS),
                UiActionBinding.action("#UpgradeButton", UiActions.OPEN_FARMSTEAD_UPGRADE),
                UiActionBinding.action("#CloseButton", UiActions.CLOSE)
        );
    }

    private static String status(Player player, PlayerGameState state, ICastleBuildingHandler buildingHandler, CastleBuildingData building) {
        CastleBuildingSummary summary = buildingHandler.summary(player.getUuid(), state, building, Instant.now());
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

