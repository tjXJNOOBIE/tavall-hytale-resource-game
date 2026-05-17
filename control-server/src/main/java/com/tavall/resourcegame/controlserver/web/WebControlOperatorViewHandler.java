package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.control.ControlOperator;

public final class WebControlOperatorViewHandler implements IWebControlOperatorViewHandler, IControlServerDomain {
    public String operatorTable() {
        StringBuilder body = new StringBuilder("<table><tr><th>Name</th><th>Role</th><th>Enabled</th></tr>");
        for (ControlOperator operator : getControlCommandRuntime().operatorRepository().findOperators()) {
            body.append("<tr><td>").append(getWebControlHtmlHandler().escape(operator.displayName())).append("</td><td>")
                    .append(getWebControlHtmlHandler().escape(operator.role().name())).append("</td><td>")
                    .append(operator.enabled()).append("</td></tr>");
        }
        body.append("</table>");
        return body.toString();
    }
}
