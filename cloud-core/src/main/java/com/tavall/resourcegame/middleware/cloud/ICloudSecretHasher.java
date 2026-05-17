package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICloudSecretHasher extends IDependencyInjectableInterface {
    String sha256(String value);
}
