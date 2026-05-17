package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public final class ControlPlayerController implements IControlServerDomain {
    @GetMapping("/control/players")
    @ResponseBody
    public String players(@RequestParam Optional<String> universalPlayerId) {
        return getWebControlHtmlHandler().page("Players", getWebControlPlayerViewHandler().playersBody(universalPlayerId));
    }
}
