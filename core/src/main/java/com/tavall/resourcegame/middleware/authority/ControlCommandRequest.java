package com.tavall.resourcegame.middleware.authority;

import java.util.Objects;
import java.util.UUID;

public record ControlCommandRequest(
        UUID requestId,
        UUID principalId,
        ControlPrincipalType principalType,
        CloudCommandType commandType,
        ResourceTarget target,
        String payloadJson,
        String reason,
        long requestedAtEpochMillis,
        boolean approvalPresent,
        boolean breakGlassActive
) {
    public ControlCommandRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(principalId, "principalId");
        principalType = principalType == null ? ControlPrincipalType.SERVICE_ACCOUNT : principalType;
        Objects.requireNonNull(commandType, "commandType");
        target = target == null ? ResourceTarget.global() : target;
        payloadJson = payloadJson == null ? "{}" : payloadJson;
        reason = reason == null ? "" : reason;
    }
}
