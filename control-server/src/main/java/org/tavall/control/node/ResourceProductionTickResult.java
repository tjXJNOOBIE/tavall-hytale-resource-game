package org.tavall.control.node;

public record ResourceProductionTickResult(
        ResourceNode resourceNode,
        int producedAmount
) {
}
