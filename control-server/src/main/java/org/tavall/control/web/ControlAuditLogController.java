package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlAuditLogController implements ControlServerDomain {
    @GetMapping("/control/audit")
    @ResponseBody
    public String auditLogs() {
        return getWebControlHtmlHandler().page("Audit Logs", getWebControlAuditQueryHandler().recentAuditTable());
    }
}
