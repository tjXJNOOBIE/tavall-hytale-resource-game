package org.tavall.control.web;

import org.tavall.control.IControlServerDomain;
import org.tavall.control.runtime.ControlCommandResult;

public final class WebControlPanelCommandHandler implements IWebControlPanelCommandHandler, IControlServerDomain {
    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        return getWebControlCommandSubmissionHandler().submitCommandLine(commandLine, dryRun);
    }
}
