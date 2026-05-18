package org.tavall.api.minecraft.frontend;

import java.util.List;

public final class FrontendKdCommandInputFormatter {
    public String rawKdInput(List<String> commandTokens) {
        if (commandTokens == null || commandTokens.isEmpty()) {
            return "/kd";
        }
        String firstToken = commandTokens.getFirst();
        if ("kd".equalsIgnoreCase(firstToken) || "kingdom".equalsIgnoreCase(firstToken)) {
            return "/" + String.join(" ", commandTokens);
        }
        return "/kd " + String.join(" ", commandTokens);
    }
}
