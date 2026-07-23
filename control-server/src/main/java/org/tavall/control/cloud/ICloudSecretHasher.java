package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface ICloudSecretHasher extends IDependencyInjectableInterface {
    String sha256(String value);
}
