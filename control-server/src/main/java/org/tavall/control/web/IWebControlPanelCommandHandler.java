package org.tavall.control.web;

import org.tavall.control.runtime.ControlCommandResult;

public interface IWebControlPanelCommandHandler {
    ControlCommandResult submitCommandLine(String commandLine, boolean dryRun);
}
