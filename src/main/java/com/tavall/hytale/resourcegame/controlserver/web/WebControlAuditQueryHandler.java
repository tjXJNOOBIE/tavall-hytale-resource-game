package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandAuditLog;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;

public final class WebControlAuditQueryHandler {
    private final ControlCommandRuntime runtime;
    private final WebControlHtmlHandler htmlHandler;

    public WebControlAuditQueryHandler(ControlCommandRuntime runtime, WebControlHtmlHandler htmlHandler) {
        this.runtime = runtime;
        this.htmlHandler = htmlHandler;
    }

    public String recentAuditTable() {
        StringBuilder builder = new StringBuilder("<table><tr><th>Command</th><th>From</th><th>State</th><th>Success</th><th>Message</th></tr>");
        for (ControlCommandAuditLog auditLog : runtime.auditLogRepository().findRecentAuditLogs(50)) {
            builder.append("<tr><td>").append(htmlHandler.escape(auditLog.commandType().name())).append("</td><td>")
                    .append(htmlHandler.escape(auditLog.issuedFrom().name())).append("</td><td>")
                    .append(htmlHandler.escape(auditLog.resultState().name())).append("</td><td>")
                    .append(auditLog.success()).append("</td><td>")
                    .append(htmlHandler.escape(auditLog.message())).append("</td></tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }
}
