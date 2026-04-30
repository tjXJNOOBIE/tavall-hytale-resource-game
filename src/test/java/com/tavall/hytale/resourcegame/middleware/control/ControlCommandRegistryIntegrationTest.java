package com.tavall.hytale.resourcegame.middleware.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlCommandRegistryIntegrationTest {
    @Test
    void commandDefinitionsExposeArgumentsPermissionsDryRunAndFanoutPolicy() {
        ControlCommandRegistry registry = new ControlCommandRegistry();

        ControlCommandDefinition woundDefinition = registry.definition(ControlCommandType.ASSIGN_TROOP_WOUND);
        ControlCommandDefinition healingDefinition = registry.definition(ControlCommandType.START_TROOP_HEALING);
        ControlCommandDefinition giveResourceDefinition = registry.definition(ControlCommandType.GIVE_RESOURCE);
        ControlCommandDefinition syncDefinition = registry.definition(ControlCommandType.SYNC_PLATFORM_STATE);

        assertEquals(ControlPermission.MANAGE_TROOP_STATE, woundDefinition.permissionRequirement().permission());
        assertTrue(woundDefinition.dryRunSupported());
        assertTrue(woundDefinition.fanout());
        assertTrue(woundDefinition.arguments().stream().anyMatch(argument -> argument.argumentName().equals("troopId") && argument.required()));
        assertTrue(healingDefinition.arguments().stream().anyMatch(argument -> argument.argumentName().equals("healingMode") && argument.required()));
        assertTrue(giveResourceDefinition.permissionRequirement().highRisk());
        assertFalse(giveResourceDefinition.fanout());
        assertFalse(syncDefinition.dryRunSupported());
        assertEquals(10, registry.definitions().size());
    }
}
