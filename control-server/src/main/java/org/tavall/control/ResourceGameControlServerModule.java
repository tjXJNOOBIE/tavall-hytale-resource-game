package org.tavall.control;

import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.logging.Log;

public final class ResourceGameControlServerModule implements ControlServerDomain {
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
