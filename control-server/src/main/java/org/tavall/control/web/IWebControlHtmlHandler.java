package org.tavall.control.web;

import org.tavall.control.runtime.ControlCommandResult;

public interface IWebControlHtmlHandler {
    String page(String title, String body);

    String renderResult(ControlCommandResult result);

    String escape(String raw);
}
