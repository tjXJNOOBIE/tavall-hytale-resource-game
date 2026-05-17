package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IIpHashService extends IDependencyInjectableInterface {
    String hash(String rawValue);
}