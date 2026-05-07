package com.tavall.hytale.resourcegame.controlserver.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlCastleController {
    private final WebControlHtmlHandler htmlHandler = new WebControlHtmlHandler();

    @GetMapping("/control/castles")
    @ResponseBody
    public String castles() {
        return htmlHandler.page("Castles and Nodes", "<p>Castle and resource-node lookup commands route through the command console.</p>");
    }
}
