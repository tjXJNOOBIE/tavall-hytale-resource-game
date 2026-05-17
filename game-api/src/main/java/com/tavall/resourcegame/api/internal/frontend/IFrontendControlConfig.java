package com.tavall.resourcegame.api.internal.frontend;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.net.URI;

public interface IFrontendControlConfig extends IDependencyInjectableInterface {
    URI controlIngressUri();

    String serverId();
}
