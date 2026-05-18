package org.tavall.control.dependency;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.tavall.control.runtime.IDebugCommandHandler;

import java.util.List;

/**
 * Test stub for command resolution.
 */
public final class TestDebugCommandHandler implements IDebugCommandHandler {
    @Override
    public List<AbstractAsyncCommand> commands() {
        return List.of();
    }
}
