package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICloudSecretHasher extends IDependencyInjectableInterface {
    String sha256(String value);
}
