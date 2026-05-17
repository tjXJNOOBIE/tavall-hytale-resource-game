package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.UUID;

public final class FirewallRuleDesiredStateHandler implements IFirewallRuleDesiredStateHandler, ICloudControlDomain {
    @Override
    public CloudFirewallRule createRule(UUID nodeId, int port, PortProtocol protocol, String source, FirewallAction action, String reason) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Firewall port must be between 1 and 65535.");
        }
        CloudFirewallRule rule = new CloudFirewallRule(UUID.randomUUID(), nodeId, port, protocol, source, action, reason,
                CloudDesiredState.DESIRED, CloudDesiredState.DESIRED, Map.of());
        getCloudRepository().saveFirewallRule(rule);
        return rule;
    }
}
