package com.tavall.hytale.resourcegame.controlserver.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlGlobalAssetController {
    private final WebControlHtmlHandler htmlHandler = new WebControlHtmlHandler();

    @GetMapping("/control/assets")
    @ResponseBody
    public String globalAssets() {
        return htmlHandler.page("Global Assets", "<p>Use the command console: asset register &lt;globalAssetId&gt; &lt;assetType&gt; &lt;displayName&gt;</p>");
    }
}
