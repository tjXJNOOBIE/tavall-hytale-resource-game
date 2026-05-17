package com.tavall.resourcegame.distribution;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.distribution.health.IRemoteHealthCheckHandler;
import com.tavall.resourcegame.distribution.node.INodeHeartbeatHandler;
import com.tavall.resourcegame.distribution.node.INodeRegistryHandler;
import com.tavall.resourcegame.distribution.remote.IDistributedTestRunnerHandler;
import com.tavall.resourcegame.distribution.remote.IRemoteCommandHandler;
import com.tavall.resourcegame.distribution.remote.IRemoteCommandPolicy;
import com.tavall.resourcegame.distribution.remote.IRemoteEnvironmentProbeHandler;

import java.time.Clock;

/**
 * Generated-domain equivalent for distributed runtime dependencies.
 */
public interface IDistributionDomainGenerated {
    default Clock getDistributionClock() {
        return DependencyLoaderAccess.findInstance(Clock.class);
    }

    default INodeRegistryHandler getNodeRegistryHandler() {
        return DependencyLoaderAccess.findInstance(INodeRegistryHandler.class);
    }

    default INodeHeartbeatHandler getNodeHeartbeatHandler() {
        return DependencyLoaderAccess.findInstance(INodeHeartbeatHandler.class);
    }

    default IRemoteCommandPolicy getRemoteCommandPolicy() {
        return DependencyLoaderAccess.findInstance(IRemoteCommandPolicy.class);
    }

    default IRemoteCommandHandler getRemoteCommandHandler() {
        return DependencyLoaderAccess.findInstance(IRemoteCommandHandler.class);
    }

    default IRemoteEnvironmentProbeHandler getRemoteEnvironmentProbeHandler() {
        return DependencyLoaderAccess.findInstance(IRemoteEnvironmentProbeHandler.class);
    }

    default IRemoteHealthCheckHandler getRemoteHealthCheckHandler() {
        return DependencyLoaderAccess.findInstance(IRemoteHealthCheckHandler.class);
    }

    default IDistributedTestRunnerHandler getDistributedTestRunnerHandler() {
        return DependencyLoaderAccess.findInstance(IDistributedTestRunnerHandler.class);
    }
}
