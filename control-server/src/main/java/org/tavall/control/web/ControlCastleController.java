package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlCastleController implements ControlServerDomain {
    @GetMapping("/control/castles")
    @ResponseBody
    public String castles() {
        return getWebControlHtmlHandler().page("Castles and Nodes", "<p>Castle and resource-node lookup commands route through the command console.</p>");
    }
}
