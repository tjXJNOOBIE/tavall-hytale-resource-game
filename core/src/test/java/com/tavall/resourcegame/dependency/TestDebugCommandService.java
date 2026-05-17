package com.tavall.resourcegame.dependency;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.tavall.resourcegame.dependency.interfaces.IDebugCommandService;

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