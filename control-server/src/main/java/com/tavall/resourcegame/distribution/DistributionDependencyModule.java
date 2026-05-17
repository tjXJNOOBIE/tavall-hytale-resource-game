package com.tavall.resourcegame.distribution;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import com.tavall.resourcegame.distribution.health.IRemoteHealthCheckHandler;
import com.tavall.resourcegame.distribution.health.RemoteHealthCheckHandler;
import com.tavall.resourcegame.distribution.node.INodeHeartbeatHandler;
import com.tavall.resourcegame.distribution.node.INodeRegistryHandler;
import com.tavall.resourcegame.distribution.node.NodeHeartbeatHandler;
import com.tavall.resourcegame.distribution.node.NodeRegistryHandler;
import com.tavall.resourcegame.distribution.remote.DistributedTestRunnerHandler;
import com.tavall.resourcegame.distribution.remote.IDistributedTestRunnerHandler;
import com.tavall.resourcegame.distribution.remote.IRemoteCommandHandler;
import com.tavall.resourcegame.distribution.remote.IRemoteCommandPolicy;
import com.tavall.resourcegame.distribution.remote.IRemoteEnvironmentProbeHandler;
import com.tavall.resourcegame.distribution.remote.RemoteCommandHandler;
import com.tavall.resourcegame.distribution.remote.RemoteCommandPolicy;
import com.tavall.resourcegame.distribution.remote.RemoteEnvironmentProbeHandler;

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
