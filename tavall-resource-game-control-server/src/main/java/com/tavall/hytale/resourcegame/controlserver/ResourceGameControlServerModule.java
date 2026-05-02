package com.tavall.hytale.resourcegame.controlserver;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tjxjnoobie.api.platform.global.console.Log;

public final class ResourceGameControlServerModule {
    public String moduleName() {
        return "tavall-resource-game-control-server";
    }

    public String canonicalCommandIngressEntryPoint() {
        return "FrontendCommandIngressHandler";
    }

    public ControlCommandRuntime createInMemoryRuntime() {
        Log.info("Creating resource game control-server runtime.");
        return ControlCommandRuntimeFactory.createInMemoryRuntime();
    }
}
