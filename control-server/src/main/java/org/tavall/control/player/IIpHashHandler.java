package org.tavall.control.player;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IIpHashHandler extends IDependencyInjectableInterface {
    String hash(String rawValue);
}
