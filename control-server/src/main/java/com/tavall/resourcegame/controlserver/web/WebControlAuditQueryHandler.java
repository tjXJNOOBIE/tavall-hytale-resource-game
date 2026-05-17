package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.control.ControlCommandAuditLog;

public final class WebControlAuditQueryHandler implements IWebControlAuditQueryHandler, IControlServerDomain {
    public String recentAuditTable() {
        StringBuilder builder = new StringBuilder("<table><tr><th>Command</th><th>From</th><th>State</th><th>Success</th><th>Message</th></tr>");
        for (ControlCommandAuditLog auditLog : getControlCommandRuntime().auditLogRepository().findRecentAuditLogs(50)) {
            builder.append("<tr><td>").append(getWebControlHtmlHandler().escape(auditLog.commandType().name())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(auditLog.issuedFrom().name())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(auditLog.resultState().name())).append("</td><td>")
                    .append(auditLog.success()).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(auditLog.message())).append("</td></tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }
}
