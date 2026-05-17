package com.tavall.resourcegame.distribution.remote;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IRemoteEnvironmentProbeHandler extends IDependencyInjectableInterface {
    RemoteEnvironmentSnapshot detectLocalEnvironment();

    RemoteEnvironmentSnapshot probeRemoteEnvironment(RemoteTarget target);
}
