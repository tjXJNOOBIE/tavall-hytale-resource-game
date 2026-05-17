package com.tavall.resourcegame.distribution.remote;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IRemoteCommandHandler extends IDependencyInjectableInterface {
    RemoteCommandResult runRemoteCommand(RemoteTarget target, RemoteCommand command);
}
