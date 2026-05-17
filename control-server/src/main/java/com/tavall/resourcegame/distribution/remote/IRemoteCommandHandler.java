package com.tavall.resourcegame.distribution.remote;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IRemoteCommandHandler extends IDependencyInjectableInterface {
    RemoteCommandResult runRemoteCommand(RemoteTarget target, RemoteCommand command);
}
