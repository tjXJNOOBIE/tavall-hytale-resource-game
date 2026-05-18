package org.tavall.control.ui;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.dependency.interfaces.IUiActionService;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.UiNavigationContext;
import org.tavall.control.resources.ResourceType;
import org.tavall.control.services.CastleEconomyPlanner;

import java.util.List;
import java.util.Map;

/**
 * Resource inventory page.
 */
public final class CastleResourcesPage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/castle-resources.html";

    public CastleResourcesPage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IUiActionService actionService,
            CastleEconomyPlanner economyPlanner
    ) {
        super(player, context, state, actionService, PAGE_DOCUMENT, templateData(state, economyPlanner), bindings());
    }

    private static Map<String, ?> templateData(PlayerGameState state, CastleEconomyPlanner economyPlanner) {
        return Map.ofEntries(
                Map.entry("FoodCount", String.valueOf(state.resources().food())),
                Map.entry("WoodCount", String.valueOf(state.resources().wood())),
                Map.entry("IronCount", String.valueOf(state.resources().iron())),
                Map.entry("FoodNodeStatus", economyPlanner.nodeSummary(state, ResourceType.FOOD)),
                Map.entry("WoodNodeStatus", economyPlanner.nodeSummary(state, ResourceType.WOOD)),
                Map.entry("IronNodeStatus", economyPlanner.nodeSummary(state, ResourceType.IRON))
        );
    }

    private static List<HyUiActionBinding> bindings() {
        return List.of(HyUiActionBinding.action("#BackButton", UiActions.OPEN_CASTLE_MAIN));
    }
}
