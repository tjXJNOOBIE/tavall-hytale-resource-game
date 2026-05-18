package org.tavall.control.cloud;

import java.util.Map;
import java.util.UUID;

public record CloudFirewallRule(
        UUID ruleId,
        UUID nodeId,
        int port,
        PortProtocol protocol,
        String source,
        FirewallAction action,
        String reason,
        CloudDesiredState desiredState,
        CloudDesiredState actualState,
        Map<String, String> metadata
) {
    public CloudFirewallRule {
        protocol = protocol == null ? PortProtocol.TCP : protocol;
        action = action == null ? FirewallAction.ALLOW : action;
        source = source == null || source.isBlank() ? "0.0.0.0/0" : source;
        reason = reason == null ? "" : reason;
        desiredState = desiredState == null ? CloudDesiredState.DESIRED : desiredState;
        actualState = actualState == null ? CloudDesiredState.DESIRED : actualState;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
