package com.tavall.hytale.resourcegame.frontend.discord;

import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionPolicy;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DiscordCommandPermissionHandlerTest {
    @Test
    void memberRoleCanRunUserLaneButNotAdminLane() {
        DiscordCommandPermissionHandler handler = new DiscordCommandPermissionHandler(
                new DiscordPermissionResolver(DiscordPermissionMapping.empty()),
                new UniversalPermissionPolicy()
        );
        DiscordPermissionContext context = new DiscordPermissionContext(
                "discord-user",
                "Citizen",
                false,
                Set.of(),
                Set.of()
        );

        assertTrue(handler.canExecute(context, DiscordCommandLane.USER));
        assertFalse(handler.canExecute(context, DiscordCommandLane.ADMIN));
    }

    @Test
    void configuredAdminRoleCanRunAdminLane() {
        DiscordBotConfig config = DiscordBotConfig.fromEnvironment(Map.of(
                "RESOURCE_GAME_DISCORD_ADMIN_ROLE_NAMES", "Kingdom Admin"
        ));
        DiscordCommandPermissionHandler handler = new DiscordCommandPermissionHandler(
                new DiscordPermissionResolver(config.permissionMapping()),
                new UniversalPermissionPolicy()
        );
        DiscordPermissionContext context = new DiscordPermissionContext(
                "discord-admin",
                "Steward",
                false,
                Set.of(),
                Set.of("Kingdom Admin")
        );

        assertTrue(handler.canExecute(context, DiscordCommandLane.ADMIN));
    }

    @Test
    void guildOwnerCanRunAdminLaneWithoutRoleMapping() {
        DiscordCommandPermissionHandler handler = new DiscordCommandPermissionHandler(
                new DiscordPermissionResolver(DiscordPermissionMapping.empty()),
                new UniversalPermissionPolicy()
        );
        DiscordPermissionContext context = new DiscordPermissionContext(
                "discord-owner",
                "Owner",
                true,
                Set.of(),
                Set.of()
        );

        assertTrue(handler.canExecute(context, DiscordCommandLane.ADMIN));
    }
}
