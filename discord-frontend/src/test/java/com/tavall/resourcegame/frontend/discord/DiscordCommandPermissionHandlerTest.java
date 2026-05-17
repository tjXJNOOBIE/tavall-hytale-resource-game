package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.DependencyLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DiscordCommandPermissionHandlerTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
    }

    @Test
    void memberRoleCanRunUserLaneButNotAdminLane() {
        new DiscordFrontendDependencyModule().registerDependencies(testConfig(DiscordPermissionMapping.empty()));
        DiscordCommandPermissionHandler handler = new DiscordCommandPermissionHandler();
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
        new DiscordFrontendDependencyModule().registerDependencies(config);
        DiscordCommandPermissionHandler handler = new DiscordCommandPermissionHandler();
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
        new DiscordFrontendDependencyModule().registerDependencies(testConfig(DiscordPermissionMapping.empty()));
        DiscordCommandPermissionHandler handler = new DiscordCommandPermissionHandler();
        DiscordPermissionContext context = new DiscordPermissionContext(
                "discord-owner",
                "Owner",
                true,
                Set.of(),
                Set.of()
        );

        assertTrue(handler.canExecute(context, DiscordCommandLane.ADMIN));
    }

    private DiscordBotConfig testConfig(DiscordPermissionMapping permissionMapping) {
        return new DiscordBotConfig(
                "test-token",
                "guild-1",
                "tcp://127.0.0.1:18081",
                permissionMapping
        );
    }
}
