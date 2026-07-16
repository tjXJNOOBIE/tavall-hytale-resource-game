package org.tavall.control.runtime;

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
        ControlCommandDefinition webPanelDefinition = registry.definition(ControlCommandType.START_CONTROL_SURFACE);
        ControlCommandDefinition createKingdomDefinition = registry.definition(ControlCommandType.CREATE_KINGDOM);
        ControlCommandDefinition playerLocationDefinition = registry.definition(ControlCommandType.UPDATE_PLAYER_LOCATION);
        ControlCommandDefinition parameterDefinition = registry.definition(ControlCommandType.UPDATE_EDITABLE_PARAMETER);

        assertEquals(ControlPermission.MANAGE_TROOP_STATE, woundDefinition.permissionRequirement().permission());
        assertTrue(woundDefinition.dryRunSupported());
        assertTrue(woundDefinition.fanout());
        assertTrue(woundDefinition.arguments().stream().anyMatch(argument -> argument.argumentName().equals("troopId") && argument.required()));
        assertTrue(healingDefinition.arguments().stream().anyMatch(argument -> argument.argumentName().equals("healingMode") && argument.required()));
        assertTrue(giveResourceDefinition.permissionRequirement().highRisk());
        assertFalse(giveResourceDefinition.fanout());
        assertFalse(syncDefinition.dryRunSupported());
        assertTrue(webPanelDefinition.permissionRequirement().highRisk());
        assertEquals(ControlPermission.MANAGE_CONTROL_OPERATORS, webPanelDefinition.permissionRequirement().permission());
        assertEquals(ControlPermission.MANAGE_KINGDOM_STATE, createKingdomDefinition.permissionRequirement().permission());
        assertTrue(createKingdomDefinition.dryRunSupported());
        assertTrue(createKingdomDefinition.fanout());
        assertTrue(playerLocationDefinition.arguments().stream().anyMatch(argument -> argument.argumentName().equals("platform") && argument.required()));
        assertTrue(parameterDefinition.permissionRequirement().highRisk());
        assertEquals(ControlCommandType.values().length, registry.definitions().size());
    }
}
