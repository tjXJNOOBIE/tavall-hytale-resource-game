package org.tavall.api.minecraft.interaction;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class InteractionContractTest {
    @Test
    void menuModelRequiresStableInventoryShape() {
        InteractionMenuModel model = new InteractionMenuModel(
                "npc-main",
                "NPC",
                27,
                InteractionTargetType.NPC,
                "npc-1",
                List.of(new InteractionMenuElement("npc-open", 11, "PAPER", "Open", List.of("Open the menu"), true, null, "npc.open", Map.of())),
                Map.of("surface", "minecraft")
        );

        assertEquals("npc-main", model.menuId());
        assertEquals(1, model.elements().size());
    }

    @Test
    void requestRejectsBlankIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new InteractionRequest("", "player-1", InteractionTargetType.UNKNOWN, "target", "click", "server", "world", Map.of(), 1L));
    }
}
