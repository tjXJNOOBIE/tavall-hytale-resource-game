package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public interface IWebControlHtmlHandler {
    String page(String title, String body);

    String renderResult(ControlCommandResult result);

    String escape(String raw);
}
