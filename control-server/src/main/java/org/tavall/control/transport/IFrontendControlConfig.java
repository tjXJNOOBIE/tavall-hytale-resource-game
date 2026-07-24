package org.tavall.control.transport;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.net.URI;

public interface IFrontendControlConfig extends IDependencyInjectableInterface {
    URI controlIngressUri();

    String serverId();
}

