package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.middleware.control.CommandTargetScope;
import com.tavall.resourcegame.middleware.control.ControlCommand;
import com.tavall.resourcegame.middleware.control.ControlCommandDefinition;
import com.tavall.resourcegame.middleware.control.ControlCommandRegistry;
import com.tavall.resourcegame.middleware.control.ControlPermission;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ControlAuthorizationHandler implements IControlAuthorizationHandler, IControlAuthorityDomain {
    /**
     * Authorization always evaluates a command policy before authority grants so C-level alone never means access.
     */
    public AuthorizationResult authorize(ControlCommandRequest request) {
        ControlPermissionPolicy policy = getPermissionPolicyRepository().requiredPolicy(request.commandType());
        return authorizeAgainstPolicy(request, policy);
    }

    public AuthorizationResult authorizeControlCommand(ControlCommand command) {
        ControlCommandDefinition definition = getControlCommandRegistry().definition(command.commandType());
        ControlPermission requiredPermission = definition.permissionRequirement().permission();
        ControlAuthorityLevel level = definition.permissionRequirement().highRisk()
                ? ControlAuthorityLevel.C2_REGIONAL_OPERATOR
                : ControlAuthorityLevel.C1_LOCAL_EXECUTOR;
        boolean requiresApproval = definition.permissionRequirement().highRisk() && command.metadata().containsKey("environment")
                && "prod".equalsIgnoreCase(command.metadata().get("environment"));
        ControlPermissionPolicy policy = new ControlPermissionPolicy(
                CloudCommandType.CONTROL_COMMAND_DISPATCH,
                level,
                requiredPermission,
                requiresApproval,
                false,
                true
        );
        ControlCommandRequest request = new ControlCommandRequest(
                UUID.randomUUID(),
                command.issuedBy().operatorId(),
                principalType(command),
                CloudCommandType.CONTROL_COMMAND_DISPATCH,
                targetForCommand(command),
                command.arguments().toString(),
                command.metadata().getOrDefault("reason", ""),
                command.createdAt().toEpochMilli(),
                Boolean.parseBoolean(command.metadata().getOrDefault("approvalPresent", "false")),
                Boolean.parseBoolean(command.metadata().getOrDefault("breakGlassActive", "false"))
        );
        return authorizeAgainstPolicy(request, policy);
    }

    public boolean hasPermission(UUID principalId, ControlPermission permission, ResourceTarget target) {
        List<ControlAuthority> authorities = getAuthorityRepository().findActiveByPrincipal(principalId, System.currentTimeMillis());
        for (ControlAuthority authority : authorities) {
            if (!authority.hasPermission(permission)) {
                continue;
            }
            if (!authority.scope().matches(target)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public void requirePermission(UUID principalId, ControlPermission permission, ResourceTarget target) {
        if (hasPermission(principalId, permission, target)) {
            return;
        }
        throw new ControlPermissionDeniedException(principalId, permission, target);
    }

    private AuthorizationResult authorizeAgainstPolicy(ControlCommandRequest request, ControlPermissionPolicy policy) {
        long now = System.currentTimeMillis();
        if (policy.requiresApproval() && !request.approvalPresent()) {
            return audit(request, AuthorizationResult.denied(request.principalId(), request.commandType(), "Command requires approval."));
        }
        if (policy.requiresBreakGlass() && !request.breakGlassActive()) {
            return audit(request, AuthorizationResult.denied(request.principalId(), request.commandType(), "Command requires active break-glass session."));
        }
        if (request.principalType() == ControlPrincipalType.AI_AGENT && !policy.aiExecutionAllowed()) {
            return audit(request, AuthorizationResult.denied(request.principalId(), request.commandType(), "AI execution is not allowed for this command."));
        }

        List<ControlAuthority> authorities = getAuthorityRepository().findActiveByPrincipal(request.principalId(), now);
        for (ControlAuthority authority : authorities) {
            if (!authority.authorityLevel().canSatisfyHumanOperationalLevel(policy.minimumAuthorityLevel())) {
                continue;
            }
            if (!authority.hasPermission(policy.requiredPermission())) {
                continue;
            }
            if (!authority.scope().matches(request.target())) {
                continue;
            }
            if (policy.requiresBreakGlass() && !authority.breakGlassAllowed()) {
                continue;
            }
            AuthorizationResult result = AuthorizationResult.allowed(request.principalId(), request.commandType(), authority);
            return audit(request, result);
        }
        return audit(request, AuthorizationResult.denied(
                request.principalId(),
                request.commandType(),
                "No active authority matched required level, permission, and scope."
        ));
    }

    private AuthorizationResult audit(ControlCommandRequest request, AuthorizationResult result) {
        getAuthorizationAuditRepository().record(new AuthorizationAuditEntry(
                UUID.randomUUID(),
                request.requestId(),
                request.principalId(),
                request.commandType(),
                result.allowed(),
                result.message(),
                System.currentTimeMillis()
        ));
        return result;
    }

    private ControlPrincipalType principalType(ControlCommand command) {
        String principalType = command.metadata().get("principalType");
        if (principalType != null && !principalType.isBlank()) {
            return ControlPrincipalType.valueOf(principalType);
        }
        return switch (command.issuedBy().role()) {
            case OWNER, ADMIN -> ControlPrincipalType.HUMAN_ADMIN;
            case SYSTEM -> ControlPrincipalType.SYSTEM_SCHEDULER;
            default -> ControlPrincipalType.SERVICE_ACCOUNT;
        };
    }

    private ResourceTarget targetForCommand(ControlCommand command) {
        AuthorityScopeType scopeType = switch (firstScope(command)) {
            case GLOBAL -> AuthorityScopeType.GLOBAL;
            case REGION -> AuthorityScopeType.REGION;
            case PLAYER -> AuthorityScopeType.WORKLOAD;
            case KINGDOM, GUILD, CASTLE, RESOURCE_NODE -> AuthorityScopeType.GAME_MODULE;
            case PLATFORM, PLATFORM_INSTANCE -> AuthorityScopeType.SERVICE;
            case TROOP, COMPANION -> AuthorityScopeType.GAME_MODULE;
            case COORDINATE_CONVERSION, PARAMETER -> AuthorityScopeType.SERVICE;
        };
        String scopeId = switch (scopeType) {
            case GLOBAL -> "*";
            case GAME_MODULE -> command.arguments().getOrDefault("kingdomId", "kingdom");
            case WORKLOAD -> command.arguments().getOrDefault("universalPlayerId", command.arguments().getOrDefault("ownerPlayerId", "player"));
            case SERVICE -> command.arguments().getOrDefault("platformInstanceId", command.arguments().getOrDefault("platform", "control-plane"));
            default -> command.commandType().name();
        };
        return new ResourceTarget(scopeType, scopeId, Map.of(AuthorityScopeType.ENVIRONMENT, command.metadata().getOrDefault("environment", "dev")));
    }

    private CommandTargetScope firstScope(ControlCommand command) {
        return getControlCommandRegistry().definition(command.commandType()).supportedTargetScopes().stream()
                .findFirst()
                .orElse(CommandTargetScope.GLOBAL);
    }
}
