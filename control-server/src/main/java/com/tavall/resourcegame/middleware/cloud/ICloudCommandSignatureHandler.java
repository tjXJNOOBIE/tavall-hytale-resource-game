package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICloudCommandSignatureHandler extends IDependencyInjectableInterface {
    boolean isSignatureRequired();

    boolean verify(CloudCommand command);

    String signatureFor(CloudCommand command);
}
