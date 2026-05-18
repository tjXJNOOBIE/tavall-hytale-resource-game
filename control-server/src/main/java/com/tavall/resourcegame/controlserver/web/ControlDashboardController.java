package org.tavall.control.web;

import org.tavall.control.IControlServerDomain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

@Controller
public class ControlDashboardController implements IControlServerDomain {
    @GetMapping("/control")
    @ResponseBody
    public String dashboard() {
        return getWebControlHtmlHandler().page("Control Dashboard", getWebControlDashboardViewHandler().dashboardBody());
    }
}
