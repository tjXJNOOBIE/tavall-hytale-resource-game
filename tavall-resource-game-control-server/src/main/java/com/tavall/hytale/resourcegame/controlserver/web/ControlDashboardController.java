package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

@Controller
public class ControlDashboardController {
    private final WebControlHtmlHandler htmlHandler;
    private final WebControlDashboardViewHandler dashboardViewHandler;

    public ControlDashboardController(ControlCommandRuntime runtime) {
        this.htmlHandler = new WebControlHtmlHandler();
        this.dashboardViewHandler = new WebControlDashboardViewHandler(runtime);
    }

    @GetMapping("/control")
    @ResponseBody
    public String dashboard() {
        return htmlHandler.page("Control Dashboard", dashboardViewHandler.dashboardBody());
    }
}
