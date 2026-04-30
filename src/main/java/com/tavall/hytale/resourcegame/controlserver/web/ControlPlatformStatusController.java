package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlPlatformStatusController {
    private final WebControlHtmlHandler htmlHandler;
    private final WebControlPlatformStatusViewHandler platformStatusViewHandler;

    public ControlPlatformStatusController(ControlCommandRuntime runtime) {
        this.htmlHandler = new WebControlHtmlHandler();
        this.platformStatusViewHandler = new WebControlPlatformStatusViewHandler(runtime, htmlHandler);
    }

    @GetMapping("/control/platforms")
    @ResponseBody
    public String platformStatus() {
        return htmlHandler.page("Platform Status", platformStatusViewHandler.platformStatusTable());
    }
}
