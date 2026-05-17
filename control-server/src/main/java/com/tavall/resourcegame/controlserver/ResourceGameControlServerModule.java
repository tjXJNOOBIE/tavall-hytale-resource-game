package com.tavall.resourcegame.controlserver;

import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tjxjnoobie.api.platform.global.console.Log;

public final class ResourceGameControlServerModule implements IControlServerDomain {
    public String moduleName() {
        return "control-server";
    }

    public String canonicalCommandIngressEntryPoint() {
        return "FrontendCommandIngressHandler";
    }

    public ControlCommandRuntime createInMemoryRuntime() {
        Log.info("Creating resource game control-server runtime.");
        new ControlServerDependencyModule().registerDependencies();
        return getControlCommandRuntime();
    }
}
