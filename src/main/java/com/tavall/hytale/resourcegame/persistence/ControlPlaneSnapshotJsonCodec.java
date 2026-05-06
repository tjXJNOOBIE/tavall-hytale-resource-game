package com.tavall.hytale.resourcegame.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;

public final class ControlPlaneSnapshotJsonCodec {
    private final ObjectMapper objectMapper;

    public ControlPlaneSnapshotJsonCodec() {
        this(new ObjectMapper()
                .configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false)
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()));
    }

    public ControlPlaneSnapshotJsonCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ControlCommandValidationException("Failed to serialize control-plane snapshot: " + exception.getMessage());
        }
    }

    public <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception exception) {
            throw new ControlCommandValidationException("Failed to read control-plane snapshot: " + exception.getMessage());
        }
    }
}
