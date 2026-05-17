package com.tavall.resourcegame.middleware.authority;

import java.util.Objects;
import java.util.UUID;

public record ControlApprovalRequest(
        UUID approvalRequestId,
        UUID requestedBy,
        CloudCommandType commandType,
        ResourceTarget target,
        String reason,
        ControlAuthorityLevel requiredApprovalLevel,
        ApprovalStatus status,
        long createdAtEpochMillis,
        long expiresAtEpochMillis
) {
    public ControlApprovalRequest {
        Objects.requireNonNull(approvalRequestId, "approvalRequestId");
        Objects.requireNonNull(requestedBy, "requestedBy");
        Objects.requireNonNull(commandType, "commandType");
        target = target == null ? ResourceTarget.global() : target;
        reason = reason == null ? "" : reason;
        Objects.requireNonNull(requiredApprovalLevel, "requiredApprovalLevel");
        status = status == null ? ApprovalStatus.PENDING : status;
    }
}
