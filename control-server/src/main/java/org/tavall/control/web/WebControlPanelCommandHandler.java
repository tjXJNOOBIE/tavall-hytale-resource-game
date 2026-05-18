package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.tavall.control.runtime.ControlCommandResult;

public final class WebControlPanelCommandHandler implements IWebControlPanelCommandHandler, ControlServerDomain {
    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        return getWebControlCommandSubmissionHandler().submitCommandLine(commandLine, dryRun);
    }
}
