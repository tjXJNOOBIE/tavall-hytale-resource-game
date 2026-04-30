package com.tavall.hytale.resourcegame.controlserver.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlGuildController {
    private final WebControlHtmlHandler htmlHandler = new WebControlHtmlHandler();

    @GetMapping("/control/guilds")
    @ResponseBody
    public String guilds() {
        return htmlHandler.page("Guilds", "<p>Guild debug commands route through the command console.</p>");
    }
}
