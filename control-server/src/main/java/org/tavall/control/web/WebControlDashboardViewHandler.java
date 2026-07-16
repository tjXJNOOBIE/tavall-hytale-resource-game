package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;

public final class WebControlDashboardViewHandler implements IWebControlDashboardViewHandler, ControlServerDomain {
    public String dashboardBody() {
        return "<p>Canonical middleware/control server is active.</p>"
                + "<p>Commands registered: " + getControlCommandRuntime().commandRegistry().definitions().size() + "</p>"
                + "<p>Recent results: " + getControlCommandRuntime().resultRepository().findRecentResults(5).size() + "</p>"
                + "<p>Recent audit logs: " + getControlCommandRuntime().auditLogRepository().findRecentAuditLogs(5).size() + "</p>"
                + "<p>Platform adapters: " + getControlCommandRuntime().fanoutHandler().platformStatuses().size() + "</p>";
    }
}
