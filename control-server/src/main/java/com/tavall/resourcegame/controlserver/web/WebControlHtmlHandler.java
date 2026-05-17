package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.resourcegame.middleware.control.PlatformCommandResult;

public final class WebControlHtmlHandler implements IWebControlHtmlHandler {
    public String page(String title, String body) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>"
                + escape(title)
                + "</title><style>body{font-family:Arial,sans-serif;margin:24px;background:#f6f7f9;color:#18202a}nav{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:18px}nav a{color:#1f4f8f;text-decoration:none;font-weight:600}section{background:#fff;border:1px solid #d8dee8;border-radius:8px;margin:14px 0;padding:14px}table{border-collapse:collapse;width:100%;background:#fff}td,th{border-bottom:1px solid #e4e8ef;padding:8px 10px;text-align:left}th{color:#465465;background:#f3f5f8}.metric-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:10px}.metric{border:1px solid #d8dee8;border-radius:8px;padding:10px;background:#f9fafc}.metric span{display:block;color:#5b6573;font-size:12px}.metric strong{font-size:24px}.ok{color:#0a6b2b}.bad{color:#a00000}textarea{width:min(720px,100%);height:96px}</style></head><body><nav>"
                + "<a href=\"/control\">Dashboard</a><a href=\"/control/cloud\">Cloud</a><a href=\"/control/commands\">Commands</a><a href=\"/control/kingdoms\">Kingdoms</a><a href=\"/control/clock\">Clock</a><a href=\"/control/platforms\">Platforms</a><a href=\"/control/players\">Players</a><a href=\"/control/guilds\">Guilds</a><a href=\"/control/castles\">Castles/Nodes</a><a href=\"/control/healing\">Troop Healing</a><a href=\"/control/assets\">Assets</a><a href=\"/control/audit\">Audit</a><a href=\"/control/operators\">Operators</a>"
                + "</nav><h1>"
                + escape(title)
                + "</h1>"
                + body
                + "</body></html>";
    }

    public String renderResult(ControlCommandResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("<section><h2>Command Result</h2><dl>");
        builder.append("<dt>Command ID</dt><dd>").append(escape(result.commandId().toString())).append("</dd>");
        builder.append("<dt>State</dt><dd>").append(escape(result.state().name())).append("</dd>");
        builder.append("<dt>Success</dt><dd>").append(result.success()).append("</dd>");
        builder.append("<dt>Message</dt><dd>").append(escape(result.message())).append("</dd>");
        builder.append("</dl>");
        if (!result.platformResults().isEmpty()) {
            builder.append("<table><tr><th>Platform</th><th>Success</th><th>Message</th></tr>");
            for (PlatformCommandResult platformResult : result.platformResults()) {
                builder.append("<tr><td>").append(escape(platformResult.platform().name())).append("</td><td>")
                        .append(platformResult.success()).append("</td><td>").append(escape(platformResult.message())).append("</td></tr>");
            }
            builder.append("</table>");
        }
        builder.append("</section>");
        return builder.toString();
    }

    public String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
