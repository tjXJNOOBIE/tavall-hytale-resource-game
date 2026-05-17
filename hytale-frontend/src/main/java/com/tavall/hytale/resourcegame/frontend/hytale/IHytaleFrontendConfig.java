package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.net.URI;

public interface IHytaleFrontendConfig extends IDependencyInjectableInterface {
    URI controlIngressUri();

    String serverId();
}
