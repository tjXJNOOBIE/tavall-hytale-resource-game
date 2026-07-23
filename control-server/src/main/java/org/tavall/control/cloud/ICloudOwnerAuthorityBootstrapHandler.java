package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICloudOwnerAuthorityBootstrapHandler extends IDependencyInjectableInterface {
    void ensureLocalOwnerAuthority(Instant now);
}
