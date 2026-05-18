package org.tavall.control.player;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IIpHashService extends IDependencyInjectableInterface {
    String hash(String rawValue);
}
