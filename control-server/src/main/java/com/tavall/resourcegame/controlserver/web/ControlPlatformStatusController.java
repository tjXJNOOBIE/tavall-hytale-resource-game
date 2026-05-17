package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlPlatformStatusController implements IControlServerDomain {
    @GetMapping("/control/platforms")
    @ResponseBody
    public String platformStatus() {
        return getWebControlHtmlHandler().page("Platform Status", getWebControlPlatformStatusViewHandler().platformStatusTable());
    }
}
