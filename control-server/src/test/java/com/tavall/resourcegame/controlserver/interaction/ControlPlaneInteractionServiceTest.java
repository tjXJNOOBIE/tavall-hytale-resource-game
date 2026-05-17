package com.tavall.resourcegame.controlserver.interaction;

import com.tavall.resourcegame.api.internal.interaction.InteractionMenuModel;
import com.tavall.resourcegame.api.internal.interaction.InteractionRequest;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionResultType;
import com.tavall.resourcegame.api.internal.interaction.InteractionTargetType;
import com.tavall.resourcegame.ui.UiActions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ControlPlaneInteractionServiceTest {
    private static final Instant NOW = Instant.parse("2026-05-14T12:00:00Z");

    @Test
    void farmerNpcOpensBackendOwnedBuildingMenu() {
        ControlPlaneInteractionService service = new ControlPlaneInteractionService();
        InteractionRequest request = request(
                "interaction-npc-open",
                InteractionTargetType.NPC,
                "npc-1",
                "open_menu",
                Map.of(
                        "npcType", "farmer",
                        "displayName", "Farmer",
                        "attachedBuildingId", "farmstead-1",
                        "buildingType", "farmstead",
                        "buildingLevel", "2"
                )
        );

        InteractionResult result = service.handle(request, NOW);

        assertEquals(InteractionResultType.OPEN_MENU, result.resultType());
        assertTrue(result.success());
        assertNotNull(result.menu());
        assertEquals(InteractionTargetType.BUILDING, result.menu().targetType());
        assertEquals("farmstead-1", result.menu().targetId());
        assertEquals("farmstead-1", result.menu().metadata().get("buildingId"));
        assertTrue(result.menu().title().contains("Farmstead"));
        assertTrue(result.menu().elements().stream().anyMatch(element -> UiActions.BUILDING_START_UPGRADE.equals(element.actionId())));
    }

    @Test
    void buildingUpgradeActionRevalidatesAndUpdatesState() {
        ControlPlaneInteractionService service = new ControlPlaneInteractionService();
        InteractionRequest upgradeRequest = request(
                "interaction-building-upgrade",
                InteractionTargetType.BUILDING,
                "farmstead-2",
                "action",
                Map.of(
                        "buildingType", "farmstead",
                        "actionId", UiActions.OPEN_BUILDING_UPGRADE,
                        "buildingLevel", "2"
                )
        );

        InteractionResult result = service.handle(upgradeRequest, NOW);

        assertEquals(InteractionResultType.EXECUTE_ACTION, result.resultType());
        assertTrue(result.success());
        assertNotNull(result.menu());
        assertEquals("3", result.menu().metadata().get("buildingLevel"));

        InteractionMenuModel inspectedMenu = service.inspectBuilding("farmstead-2", NOW).menu();
        assertNotNull(inspectedMenu);
        assertEquals("3", inspectedMenu.metadata().get("buildingLevel"));
    }

    @Test
    void missingNpcReturnsNotFound() {
        ControlPlaneInteractionService service = new ControlPlaneInteractionService();

        InteractionResult result = service.inspectNpc("missing-npc", NOW);

        assertEquals(InteractionResultType.NOT_FOUND, result.resultType());
        assertFalse(result.success());
        assertTrue(result.message().contains("NPC was not found"));
    }

    @Test
    void disabledMenuElementsIncludeReasons() {
        ControlPlaneInteractionService service = new ControlPlaneInteractionService();
        InteractionResult result = service.handle(
                request(
                        "interaction-building-open",
                        InteractionTargetType.BUILDING,
                        "building-1",
                        "open_menu",
                        Map.of(
                                "buildingType", "building",
                                "buildingLevel", "1"
                        )
                ),
                NOW
        );

        assertEquals(InteractionResultType.OPEN_MENU, result.resultType());
        assertNotNull(result.menu());
        assertTrue(result.menu().elements().stream()
                .filter(element -> !element.enabled())
                .allMatch(element -> element.disabledReason() != null && !element.disabledReason().isBlank()));
    }

    private InteractionRequest request(String requestId, InteractionTargetType targetType, String targetId, String interactionType, Map<String, String> context) {
        return new InteractionRequest(
                requestId,
                "player-1",
                targetType,
                targetId,
                interactionType,
                "server-1",
                "world-1",
                context,
                NOW.toEpochMilli()
        );
    }
}
