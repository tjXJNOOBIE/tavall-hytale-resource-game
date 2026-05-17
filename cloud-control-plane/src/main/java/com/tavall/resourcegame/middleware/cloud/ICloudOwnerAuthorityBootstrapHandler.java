package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICloudOwnerAuthorityBootstrapHandler extends IDependencyInjectableInterface {
    void ensureLocalOwnerAuthority(Instant now);
}
