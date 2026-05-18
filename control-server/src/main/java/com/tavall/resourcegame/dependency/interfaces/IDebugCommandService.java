package org.tavall.control.dependency.interfaces;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IDebugCommandService extends IDependencyInjectableInterface {
    List<AbstractAsyncCommand> commands();
}