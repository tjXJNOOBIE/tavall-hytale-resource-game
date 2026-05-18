package org.tavall.control.web;

import org.tavall.control.IControlServerDomain;
import org.tavall.control.runtime.PlatformConnectionStatus;

public final class WebControlPlatformStatusViewHandler implements IWebControlPlatformStatusViewHandler, IControlServerDomain {
    public String platformStatusTable() {
        StringBuilder builder = new StringBuilder("<table><tr><th>Platform</th><th>Connected</th><th>Message</th></tr>");
        for (PlatformConnectionStatus status : getControlCommandRuntime().fanoutHandler().platformStatuses()) {
            builder.append("<tr><td>").append(getWebControlHtmlHandler().escape(status.platform().name())).append("</td><td>")
                    .append(status.connected()).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(status.message())).append("</td></tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }
}
