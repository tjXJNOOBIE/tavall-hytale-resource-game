package org.tavall.control.runtime;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IDebugCommandHandler extends IDependencyInjectableInterface {
    List<AbstractAsyncCommand> commands();
}
