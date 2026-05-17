package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class ControlOperatorController implements IControlServerDomain {
    @GetMapping("/control/operators")
    @ResponseBody
    public String operators() {
        return getWebControlHtmlHandler().page("Operators", getWebControlOperatorViewHandler().operatorTable());
    }
}
