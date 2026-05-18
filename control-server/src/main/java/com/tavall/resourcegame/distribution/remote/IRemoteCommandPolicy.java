package org.tavall.control.distribution.remote;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IRemoteCommandPolicy extends IDependencyInjectableInterface {
    List<String> validate(RemoteCommand command);
}
