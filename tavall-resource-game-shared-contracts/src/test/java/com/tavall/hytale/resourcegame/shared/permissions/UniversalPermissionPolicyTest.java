package com.tavall.hytale.resourcegame.shared.permissions;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UniversalPermissionPolicyTest {
    @Test
    void memberCanRunUserCommandsButNotAdminCommands() {
        UniversalPermissionPolicy policy = new UniversalPermissionPolicy();
        UniversalPermissionSubject subject = new UniversalPermissionSubject(
                ResourceGameFrontendPlatform.DISCORD,
                "discord-user",
                "Player",
                UniversalPermissionRole.MEMBER,
                Set.of()
        );

        assertTrue(policy.canExecuteUserCommand(subject));
        assertFalse(policy.canExecuteAdminCommand(subject));
    }

    @Test
    void adminCanRunAdminCommandsThroughRolePowerAndPermissions() {
        UniversalPermissionPolicy policy = new UniversalPermissionPolicy();
        UniversalPermissionSubject subject = new UniversalPermissionSubject(
                ResourceGameFrontendPlatform.DISCORD,
                "discord-admin",
                "Admin",
                UniversalPermissionRole.ADMIN,
                Set.of()
        );

        assertTrue(policy.canExecuteAdminCommand(subject));
        assertTrue(policy.hasPowerAtLeast(subject, UniversalPermissionRole.MODERATOR));
    }

    @Test
    void explicitPermissionCanGrantOneAdminCapabilityWithoutChangingRole() {
        UniversalPermissionPolicy policy = new UniversalPermissionPolicy();
        UniversalPermissionSubject subject = new UniversalPermissionSubject(
                ResourceGameFrontendPlatform.DISCORD,
                "discord-user",
                "Trusted Operator",
                UniversalPermissionRole.MEMBER,
                Set.of(UniversalPermission.BROADCAST_GLOBAL_MESSAGE)
        );

        assertTrue(policy.hasPermission(subject, UniversalPermission.BROADCAST_GLOBAL_MESSAGE));
        assertFalse(policy.canExecuteAdminCommand(subject));
    }
}
