package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;

public final class WebControlDashboardViewHandler {
    private final ControlCommandRuntime runtime;

    public WebControlDashboardViewHandler(ControlCommandRuntime runtime) {
        this.runtime = runtime;
    }

    public String dashboardBody() {
        return "<p>Canonical middleware/control server is active.</p>"
                + "<p>Commands registered: " + runtime.commandRegistry().definitions().size() + "</p>"
                + "<p>Recent results: " + runtime.resultRepository().findRecentResults(5).size() + "</p>"
                + "<p>Recent audit logs: " + runtime.auditLogRepository().findRecentAuditLogs(5).size() + "</p>"
                + "<p>Platform adapters: " + runtime.fanoutHandler().platformStatuses().size() + "</p>";
    }
}
