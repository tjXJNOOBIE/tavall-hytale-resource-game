package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlPlatformStatusController implements ControlServerDomain {
    @GetMapping("/control/platforms")
    @ResponseBody
    public String platformStatus() {
        return getWebControlHtmlHandler().page("Platform Status", getWebControlPlatformStatusViewHandler().platformStatusTable());
    }
}
