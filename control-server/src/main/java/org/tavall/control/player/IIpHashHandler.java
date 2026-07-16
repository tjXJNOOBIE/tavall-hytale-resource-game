package org.tavall.control.player;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IIpHashHandler extends IDependencyInjectableInterface {
    String hash(String rawValue);
}
