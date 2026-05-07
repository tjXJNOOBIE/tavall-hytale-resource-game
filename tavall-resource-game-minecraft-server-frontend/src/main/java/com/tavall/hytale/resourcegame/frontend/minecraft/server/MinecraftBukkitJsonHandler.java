package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class MinecraftBukkitJsonHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String writeJson(Object value) throws IOException {
        return objectMapper.writeValueAsString(value);
    }
}
