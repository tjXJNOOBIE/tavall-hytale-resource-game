package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IFirewallRuleDesiredStateHandler extends IDependencyInjectableInterface {
    CloudFirewallRule createRule(UUID nodeId, int port, PortProtocol protocol, String source, FirewallAction action, String reason);
}
