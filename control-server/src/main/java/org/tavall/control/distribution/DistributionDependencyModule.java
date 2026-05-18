package org.tavall.control.distribution;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import org.tavall.control.distribution.health.IRemoteHealthCheckHandler;
import org.tavall.control.distribution.health.RemoteHealthCheckHandler;
import org.tavall.control.distribution.node.INodeHeartbeatHandler;
import org.tavall.control.distribution.node.INodeRegistryHandler;
import org.tavall.control.distribution.node.NodeHeartbeatHandler;
import org.tavall.control.distribution.node.NodeRegistryHandler;
import org.tavall.control.distribution.remote.DistributedTestRunnerHandler;
import org.tavall.control.distribution.remote.IDistributedTestRunnerHandler;
import org.tavall.control.distribution.remote.IRemoteCommandHandler;
import org.tavall.control.distribution.remote.IRemoteCommandPolicy;
import org.tavall.control.distribution.remote.IRemoteEnvironmentProbeHandler;
import org.tavall.control.distribution.remote.RemoteCommandHandler;
import org.tavall.control.distribution.remote.RemoteCommandPolicy;
import org.tavall.control.distribution.remote.RemoteEnvironmentProbeHandler;

import java.time.Clock;

public final class DistributionDependencyModule implements IDependencyModule, IDistributionDomain {
    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(Clock.class, Clock.systemUTC());
        DependencyLoaderAccess.registerInstance(INodeRegistryHandler.class, new NodeRegistryHandler());
        DependencyLoaderAccess.registerInstance(INodeHeartbeatHandler.class, new NodeHeartbeatHandler());
        DependencyLoaderAccess.registerInstance(IRemoteCommandPolicy.class, RemoteCommandPolicy.defaults());
        DependencyLoaderAccess.registerInstance(IRemoteCommandHandler.class, new RemoteCommandHandler());
        DependencyLoaderAccess.registerInstance(IRemoteEnvironmentProbeHandler.class, new RemoteEnvironmentProbeHandler());
        DependencyLoaderAccess.registerInstance(IRemoteHealthCheckHandler.class, new RemoteHealthCheckHandler());
        DependencyLoaderAccess.registerInstance(IDistributedTestRunnerHandler.class, new DistributedTestRunnerHandler());
    }
}
