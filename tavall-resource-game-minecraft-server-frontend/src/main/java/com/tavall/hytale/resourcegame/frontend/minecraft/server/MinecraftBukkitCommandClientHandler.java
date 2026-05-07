package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MinecraftBukkitCommandClientHandler {
    private final URI commandIngressUri;
    private final MinecraftBukkitJsonHandler jsonHandler;

    public MinecraftBukkitCommandClientHandler(URI commandIngressUri, MinecraftBukkitJsonHandler jsonHandler) {
        this.commandIngressUri = commandIngressUri;
        this.jsonHandler = jsonHandler;
    }

    public boolean submitInteraction(
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> arguments,
            String correlationId,
            Map<String, String> sourceMetadata
    ) throws IOException {
        Map<String, Object> envelope = new LinkedHashMap<String, Object>();
        envelope.put("platform", "MINECRAFT");
        envelope.put("surface", "ENTITY_INTERACT");
        envelope.put("platformAccountId", platformAccountId);
        envelope.put("platformDisplayName", platformDisplayName);
        envelope.put("rawInput", null);
        envelope.put("actionId", actionId);
        envelope.put("arguments", arguments);
        envelope.put("correlationId", correlationId);
        envelope.put("sourceMetadata", sourceMetadata);
        byte[] payload = jsonHandler.writeJson(envelope).getBytes(StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection) commandIngressUri.toURL().openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(payload);
        }
        int status = connection.getResponseCode();
        return status >= 200 && status < 300;
    }
}
