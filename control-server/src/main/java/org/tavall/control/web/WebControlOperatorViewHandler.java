package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.tavall.control.runtime.ControlOperator;

public final class WebControlOperatorViewHandler implements IWebControlOperatorViewHandler, ControlServerDomain {
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
