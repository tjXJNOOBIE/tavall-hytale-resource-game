package com.tavall.hytale.resourcegame.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CustomEntitySpawnRoleTest {
    @Test
    void parsesBuildingControllerAliases() {
        assertEquals(CustomEntitySpawnRole.FARMSTEAD_STEWARD, CustomEntitySpawnRole.parse("steward"));
        assertEquals(CustomEntitySpawnRole.FARMSTEAD_STEWARD, CustomEntitySpawnRole.parse("farmstead steward"));
        assertEquals(CustomEntitySpawnRole.FARMER, CustomEntitySpawnRole.parse("farmer"));
        assertEquals(CustomEntitySpawnRole.LUMBERJACK, CustomEntitySpawnRole.parse("lumber-mill"));
        assertEquals(CustomEntitySpawnRole.IRONMASTER, CustomEntitySpawnRole.parse("iron works"));
    }

    @Test
    void parsesCitizenRoleAliases() {
        assertEquals(CustomEntitySpawnRole.MINER, CustomEntitySpawnRole.parse("miner"));
        assertEquals(CustomEntitySpawnRole.SOLDIER, CustomEntitySpawnRole.parse("soldier"));
    }

    @Test
    void exposesCommandChoicesForUsageText() {
        assertTrue(CustomEntitySpawnRole.commandChoices().contains("farmer"));
        assertTrue(CustomEntitySpawnRole.commandChoices().contains("soldier"));
        assertNull(CustomEntitySpawnRole.parse("unknown"));
    }
}
