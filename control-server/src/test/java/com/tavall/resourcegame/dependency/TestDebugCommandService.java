package org.tavall.control.dependency;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.tavall.control.dependency.interfaces.IDebugCommandService;

import java.util.List;

/**
 * Test stub for command resolution.
 */
public final class TestDebugCommandService implements IDebugCommandService {
    @Override
    public List<AbstractAsyncCommand> commands() {
        return List.of();
    }
}