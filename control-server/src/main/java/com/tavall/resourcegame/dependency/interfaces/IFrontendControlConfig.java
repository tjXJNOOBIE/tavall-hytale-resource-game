package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.net.URI;

public interface IFrontendControlConfig extends IDependencyInjectableInterface {
    URI controlIngressUri();

    String serverId();
}
