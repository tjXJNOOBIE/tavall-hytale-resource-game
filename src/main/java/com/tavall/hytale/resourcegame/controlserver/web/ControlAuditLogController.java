package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlAuditLogController {
    private final WebControlHtmlHandler htmlHandler;
    private final WebControlAuditQueryHandler auditQueryHandler;

    public ControlAuditLogController(ControlCommandRuntime runtime) {
        this.htmlHandler = new WebControlHtmlHandler();
        this.auditQueryHandler = new WebControlAuditQueryHandler(runtime, htmlHandler);
    }

    @GetMapping("/control/audit")
    @ResponseBody
    public String auditLogs() {
        return htmlHandler.page("Audit Logs", auditQueryHandler.recentAuditTable());
    }
}
