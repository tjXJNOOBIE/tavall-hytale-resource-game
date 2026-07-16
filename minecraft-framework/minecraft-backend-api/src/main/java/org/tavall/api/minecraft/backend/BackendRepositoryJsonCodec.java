package org.tavall.api.minecraft.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BackendRepositoryJsonCodec {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String writeStringList(List<String> values) {
        return writeJson(values == null ? List.of() : values);
    }

    public List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
        return readJson(json, type);
    }

    public String writeStringMap(Map<String, String> values) {
        return writeJson(values == null ? Map.of() : values);
    }

    public Map<String, String> readStringMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        JavaType type = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, String.class);
        return Map.copyOf(readJson(json, type));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize backend repository JSON: " + exception.getMessage(), exception);
        }
    }

    private <T> T readJson(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read backend repository JSON: " + exception.getMessage(), exception);
        }
    }
}
