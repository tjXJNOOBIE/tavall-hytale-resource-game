package org.tavall.control.interaction;

import org.tavall.api.minecraft.interaction.InteractionMenuModel;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionResultType;
import org.tavall.api.minecraft.interaction.InteractionTargetType;
import org.tavall.minecraft.framework.game.ui.UiActions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ControlPlaneInteractionHandlerTest {
    private static final Instant NOW = Instant.parse("2026-05-14T12:00:00Z");

    @Test
    void farmerNpcOpensBackendOwnedBuildingMenu() {
        ControlPlaneInteractionHandler service = new ControlPlaneInteractionHandler();
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
        ControlPlaneInteractionHandler service = new ControlPlaneInteractionHandler();
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
        ControlPlaneInteractionHandler service = new ControlPlaneInteractionHandler();

        InteractionResult result = service.inspectNpc("missing-npc", NOW);

        assertEquals(InteractionResultType.NOT_FOUND, result.resultType());
        assertFalse(result.success());
        assertTrue(result.message().contains("NPC was not found"));
    }

    @Test
    void disabledMenuElementsIncludeReasons() {
        ControlPlaneInteractionHandler service = new ControlPlaneInteractionHandler();
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
