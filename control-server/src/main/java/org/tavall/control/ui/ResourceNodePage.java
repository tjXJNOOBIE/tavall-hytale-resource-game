package org.tavall.control.ui;

import org.tavall.api.minecraft.ui.UiActions;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.resource.IResourceNodeHandler;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.domain.ResourceNodeData;
import org.tavall.control.domain.ResourceNodeSummary;
import org.tavall.control.domain.UiNavigationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UI for assigning troop counts to a selected resource node.
 */
public final class ResourceNodePage extends BaseUiPage {
    private static final String PAGE_DOCUMENT = "Pages/resource-node-detail.html";

    public ResourceNodePage(
            Player player,
            UiNavigationContext context,
            PlayerGameState state,
            IResourceNodeHandler resourceNodeHandler
    ) {
        super(player, context, state, PAGE_DOCUMENT, templateData(context, state, resourceNodeHandler), bindings());
    }

    private static Map<String, ?> templateData(UiNavigationContext context, PlayerGameState state, IResourceNodeHandler resourceNodeHandler) {
        Optional<ResourceNodeData> nodeOptional = resourceNodeHandler.findNode(state, context.selectedNodeId());
        if (nodeOptional.isEmpty()) {
            return Map.ofEntries(
                    Map.entry("NodeTitle", "Node not found"),
                    Map.entry("NodeSummary", "Select a node from /kd nodes list or click a node in-world."),
                    Map.entry("AssignedTroops", "0"),
                    Map.entry("AssignedWorkers", "0"),
                    Map.entry("AvailableTroops", String.valueOf(resourceNodeHandler.availableTroops(state))),
                    Map.entry("GainPerTick", "+0/tick"),
                    Map.entry("PillageReward", "+0"),
                    Map.entry("StockStatus", "0 / 0 (0%)"),
                    Map.entry("RegenStatus", "+0 / tick"),
                    Map.entry("StatusText", "Exhausted"),
                    Map.entry("RouteStatus", "No supply lane"),
                    Map.entry("FeedbackStatus", "No node selected.")
            );
        }
        ResourceNodeData node = nodeOptional.get();
        ResourceNodeSummary summary = resourceNodeHandler.summary(state, node);
        return Map.ofEntries(
                Map.entry("NodeTitle", node.resourceType() + " Node " + node.nodeId().toString().substring(0, 8)),
                Map.entry("NodeSummary", node.worldName() + " | " + (int) node.x() + ", " + (int) node.y() + ", " + (int) node.z()),
                Map.entry("AssignedTroops", String.valueOf(summary.assignedTroops())),
                Map.entry("AssignedWorkers", String.valueOf(summary.assignedWorkers())),
                Map.entry("AvailableTroops", String.valueOf(summary.availableTroops())),
                Map.entry("GainPerTick", "+" + summary.gainPerTick() + "/tick (" + summary.workerGainPerTick() + " worker, " + summary.troopGainPerTick() + " troop)"),
                Map.entry("PillageReward", "+" + summary.pillageReward()),
                Map.entry("StockStatus", summary.currentStock() + " / " + summary.maxStock() + " (" + summary.stockPercent() + "%)"),
                Map.entry("RegenStatus", "+" + summary.regenerationPerTick() + " / tick"),
                Map.entry("StatusText", summary.stockStatus()),
                Map.entry("RouteStatus", summary.visibleRouteCount() <= 0 ? "No supply lane" : "Supply lane active: " + summary.visibleRouteCount() + " convoy markers"),
                Map.entry("FeedbackStatus", context.feedbackMessage().isBlank() ? "Workers auto-gather here. Troops can be sent or used for a larger manual pillage." : context.feedbackMessage())
        );
    }

    private static List<UiActionBinding> bindings() {
        return List.of(
                UiActionBinding.action("#AssignOneButton", UiActions.NODE_ASSIGN_ONE),
                UiActionBinding.action("#AssignThreeButton", UiActions.NODE_ASSIGN_THREE),
                UiActionBinding.action("#AssignFiveButton", UiActions.NODE_ASSIGN_FIVE),
                UiActionBinding.action("#AssignAllButton", UiActions.NODE_ASSIGN_ALL),
                UiActionBinding.action("#RecallOneButton", UiActions.NODE_RECALL_ONE),
                UiActionBinding.action("#RecallAllButton", UiActions.NODE_RECALL_ALL),
                UiActionBinding.action("#PillageButton", UiActions.NODE_PILLAGE),
                UiActionBinding.action("#BackButton", UiActions.OPEN_RESOURCES)
        );
    }
}

