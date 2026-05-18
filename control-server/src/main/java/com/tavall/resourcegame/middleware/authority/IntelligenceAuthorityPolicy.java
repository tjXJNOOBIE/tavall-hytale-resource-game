package org.tavall.control.authority;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record IntelligenceAuthorityPolicy(
        UUID principalId,
        IntelligenceExecutionMode executionMode,
        Set<CloudCommandType> allowedCommandTypes,
        Set<AuthorityScope> allowedScopes,
        boolean requiresHumanApprovalForDestructiveActions,
        long maxResourceChangePercent
) {
    public IntelligenceAuthorityPolicy {
        Objects.requireNonNull(principalId, "principalId");
        executionMode = executionMode == null ? IntelligenceExecutionMode.ADVISORY_ONLY : executionMode;
        allowedCommandTypes = allowedCommandTypes == null ? Set.of() : Set.copyOf(allowedCommandTypes);
        allowedScopes = allowedScopes == null ? Set.of() : Set.copyOf(allowedScopes);
    }
}
