package org.tavall.control.distribution;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.distribution.health.IRemoteHealthCheckHandler;
import org.tavall.control.distribution.node.INodeHeartbeatHandler;
import org.tavall.control.distribution.node.INodeRegistryHandler;
import org.tavall.control.distribution.remote.IDistributedTestRunnerHandler;
import org.tavall.control.distribution.remote.IRemoteCommandHandler;
import org.tavall.control.distribution.remote.IRemoteCommandPolicy;
import org.tavall.control.distribution.remote.IRemoteEnvironmentProbeHandler;

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
