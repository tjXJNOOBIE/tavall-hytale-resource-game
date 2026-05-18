package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICloudOwnerAuthorityBootstrapHandler extends IDependencyInjectableInterface {
    void ensureLocalOwnerAuthority(Instant now);
}
