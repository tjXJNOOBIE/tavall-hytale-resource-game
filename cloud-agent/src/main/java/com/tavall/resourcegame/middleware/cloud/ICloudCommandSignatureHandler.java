package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICloudCommandSignatureHandler extends IDependencyInjectableInterface {
    boolean isSignatureRequired();

    boolean verify(CloudCommand command);

    String signatureFor(CloudCommand command);
}
