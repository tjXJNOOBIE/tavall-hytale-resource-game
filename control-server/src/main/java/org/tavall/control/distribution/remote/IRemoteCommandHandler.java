package org.tavall.control.distribution.remote;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IRemoteCommandHandler extends IDependencyInjectableInterface {
    RemoteCommandResult runRemoteCommand(RemoteTarget target, RemoteCommand command);
}
