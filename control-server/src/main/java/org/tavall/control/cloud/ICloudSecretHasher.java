package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICloudSecretHasher extends IDependencyInjectableInterface {
    String sha256(String value);
}
