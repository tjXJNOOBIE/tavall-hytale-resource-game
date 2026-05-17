package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlCloudController implements IControlServerDomain {
    @GetMapping("/control/cloud")
    @ResponseBody
    public String cloud() {
        return getWebControlHtmlHandler().page("Tavall Cloud", getCloudControlPanelViewHandler().cloudBody());
    }
}
