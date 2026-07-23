package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface ICloudCommandSignatureHandler extends IDependencyInjectableInterface {
    boolean isSignatureRequired();

    boolean verify(CloudCommand command);

    String signatureFor(CloudCommand command);
}
