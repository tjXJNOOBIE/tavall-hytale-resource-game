package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlOperatorController {
    private final ControlCommandRuntime runtime;
    private final WebControlHtmlHandler htmlHandler = new WebControlHtmlHandler();

    public ControlOperatorController(ControlCommandRuntime runtime) {
        this.runtime = runtime;
    }

    @GetMapping("/control/operators")
    @ResponseBody
    public String operators() {
        StringBuilder body = new StringBuilder("<table><tr><th>Name</th><th>Role</th><th>Enabled</th></tr>");
        for (ControlOperator operator : runtime.operatorRepository().findOperators()) {
            body.append("<tr><td>").append(htmlHandler.escape(operator.displayName())).append("</td><td>")
                    .append(htmlHandler.escape(operator.role().name())).append("</td><td>")
                    .append(operator.enabled()).append("</td></tr>");
        }
        body.append("</table>");
        return htmlHandler.page("Operators", body.toString());
    }
}
