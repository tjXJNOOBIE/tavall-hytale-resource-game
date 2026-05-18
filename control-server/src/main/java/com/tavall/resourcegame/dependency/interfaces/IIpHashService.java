package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IIpHashService extends IDependencyInjectableInterface {
    String hash(String rawValue);
}