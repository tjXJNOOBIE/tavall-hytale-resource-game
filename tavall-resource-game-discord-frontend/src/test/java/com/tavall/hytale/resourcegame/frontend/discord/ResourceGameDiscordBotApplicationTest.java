package com.tavall.hytale.resourcegame.frontend.discord;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourceGameDiscordBotApplicationTest {
    @Test
    void configUsesLocalControlIngressByDefault() {
        ResourceGameDiscordBotApplicationConfigProbe probe = ResourceGameDiscordBotApplication.configProbe(Map.of(
                "RESOURCE_GAME_DISCORD_BOT_TOKEN", "redacted-token"
        ));

        assertTrue(probe.hasToken());
        assertEquals("http://localhost:8080/api/frontend/commands", probe.controlIngressUrl());
    }

    @Test
    void configReadsProjectNovusDiscordPermissionMappings() {
        ResourceGameDiscordBotApplicationConfigProbe probe = ResourceGameDiscordBotApplication.configProbe(Map.of(
                "RESOURCE_GAME_DISCORD_OWNER_USER_IDS", "owner-1",
                "RESOURCE_GAME_DISCORD_ADMIN_ROLE_IDS", "admin-role",
                "RESOURCE_GAME_DISCORD_MODERATOR_ROLE_NAMES", "Moderator",
                "RESOURCE_GAME_CONTROL_INGRESS_URL", "https://control.project-novus.example/api/frontend/commands"
        ));

        assertFalse(probe.hasToken());
        assertEquals("https://control.project-novus.example/api/frontend/commands", probe.controlIngressUrl());
        assertTrue(probe.permissionMapping().ownerUserIds().contains("owner-1"));
        assertTrue(probe.permissionMapping().adminRoleIds().contains("admin-role"));
        assertTrue(probe.permissionMapping().moderatorRoleNames().contains("Moderator"));
    }
}
