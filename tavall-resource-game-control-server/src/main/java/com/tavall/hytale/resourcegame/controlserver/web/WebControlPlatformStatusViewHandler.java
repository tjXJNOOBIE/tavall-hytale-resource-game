package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.PlatformConnectionStatus;

public final class WebControlPlatformStatusViewHandler {
    private final ControlCommandRuntime runtime;
    private final WebControlHtmlHandler htmlHandler;

    public WebControlPlatformStatusViewHandler(ControlCommandRuntime runtime, WebControlHtmlHandler htmlHandler) {
        this.runtime = runtime;
        this.htmlHandler = htmlHandler;
    }

    public String platformStatusTable() {
        StringBuilder builder = new StringBuilder("<table><tr><th>Platform</th><th>Connected</th><th>Message</th></tr>");
        for (PlatformConnectionStatus status : runtime.fanoutHandler().platformStatuses()) {
            builder.append("<tr><td>").append(htmlHandler.escape(status.platform().name())).append("</td><td>")
                    .append(status.connected()).append("</td><td>")
                    .append(htmlHandler.escape(status.message())).append("</td></tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }
}
