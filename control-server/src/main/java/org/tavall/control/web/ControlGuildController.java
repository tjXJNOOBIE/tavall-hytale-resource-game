package org.tavall.control.web;

import org.tavall.control.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlGuildController implements IControlServerDomain {
    @GetMapping("/control/guilds")
    @ResponseBody
    public String guilds() {
        return getWebControlHtmlHandler().page("Guilds", "<p>Guild debug commands route through the command console.</p>");
    }
}
