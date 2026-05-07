package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class MinecraftBukkitSnapshotClientHandler {
    private final URI snapshotIngressUri;
    private final MinecraftBukkitJsonHandler jsonHandler;

    public MinecraftBukkitSnapshotClientHandler(URI snapshotIngressUri, MinecraftBukkitJsonHandler jsonHandler) {
        this.snapshotIngressUri = snapshotIngressUri;
        this.jsonHandler = jsonHandler;
    }

    public boolean submitSnapshot(MinecraftBukkitServerSnapshot snapshot) throws IOException {
        byte[] payload = jsonHandler.writeJson(snapshot).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) snapshotIngressUri.toURL().openConnection();
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
