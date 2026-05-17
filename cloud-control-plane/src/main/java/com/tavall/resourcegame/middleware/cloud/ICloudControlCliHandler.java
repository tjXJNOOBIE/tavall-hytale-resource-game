package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface ICloudControlCliHandler extends IDependencyInjectableInterface {
    String execute(String input, UUID requestedBy, Instant now);
}
