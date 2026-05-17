package com.tavall.resourcegame.distribution.remote;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IRemoteCommandPolicy extends IDependencyInjectableInterface {
    List<String> validate(RemoteCommand command);
}
