package com.tavall.resourcegame.distribution.health;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.distribution.remote.RemoteTarget;

import java.time.Duration;

public interface IRemoteHealthCheckHandler extends IDependencyInjectableInterface {
    RemoteHealthCheckResult checkRemoteServerHealth(RemoteTarget target);

    boolean verifyRedisConnectivity(RemoteTarget target);

    boolean verifyPostgresConnectivity(RemoteTarget target);

    boolean verifyHytaleServerReachable(RemoteTarget target);

    boolean isPortReachable(String host, int port, Duration timeout);
}
