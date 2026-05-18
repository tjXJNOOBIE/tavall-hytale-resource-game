package org.tavall.control.web;

import org.tavall.control.runtime.ControlCommandResult;

public interface IWebControlCommandSubmissionHandler {
    ControlCommandResult submitCommandLine(String commandLine, boolean dryRun);
}
