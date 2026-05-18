package org.tavall.control.web;

import org.tavall.control.IControlServerDomain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControlGlobalAssetController implements IControlServerDomain {
    @GetMapping("/control/assets")
    @ResponseBody
    public String globalAssets() {
        return getWebControlHtmlHandler().page("Global Assets", "<p>Use the command console: asset register &lt;globalAssetId&gt; &lt;assetType&gt; &lt;displayName&gt;</p>");
    }
}
