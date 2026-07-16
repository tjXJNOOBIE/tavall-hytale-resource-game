package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlCloudController implements ControlServerDomain {
    @GetMapping("/control/cloud")
    @ResponseBody
    public String cloud() {
        return getWebControlHtmlHandler().page("Tavall Cloud", getCloudControlPanelViewHandler().cloudBody());
    }
}
