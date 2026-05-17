package com.tavall.resourcegame.middleware.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ControlRepositoryJsonCodec {
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

    public String writePlatforms(Set<GamePlatform> platforms) {
        List<String> names = platforms == null
                ? List.of()
                : platforms.stream().map(GamePlatform::name).sorted().toList();
        return writeStringList(names);
    }

    public Set<GamePlatform> readPlatforms(String json) {
        return readStringList(json).stream()
                .map(GamePlatform::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }

    public String writePlatformResults(List<PlatformCommandResult> platformResults) {
        List<Map<String, Object>> rows = platformResults == null
                ? List.of()
                : platformResults.stream().map(this::platformResultToMap).toList();
        return writeJson(rows);
    }

    public List<PlatformCommandResult> readPlatformResults(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        JavaType mapType = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class);
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, mapType);
        List<Map<String, Object>> rows = readJson(json, listType);
        return rows.stream().map(this::platformResultFromMap).toList();
    }

    private Map<String, Object> platformResultToMap(PlatformCommandResult result) {
        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("platform", result.platform().name());
        row.put("success", result.success());
        row.put("message", result.message());
        row.put("frontendEventIds", result.frontendEventIds());
        row.put("projectionIds", result.projectionIds());
        row.put("metadata", result.metadata());
        return row;
    }

    private PlatformCommandResult platformResultFromMap(Map<String, Object> row) {
        return new PlatformCommandResult(
                GamePlatform.valueOf(asString(row.get("platform"))),
                Boolean.parseBoolean(asString(row.get("success"))),
                asString(row.get("message")),
                asStringList(row.get("frontendEventIds")),
                asStringList(row.get("projectionIds")),
                asStringMap(row.get("metadata"))
        );
    }

    private List<String> asStringList(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().map(this::asString).toList();
    }

    private Map<String, String> asStringMap(Object value) {
        if (!(value instanceof Map<?, ?> values)) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        values.forEach((key, entryValue) -> result.put(asString(key), asString(entryValue)));
        return Map.copyOf(result);
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ControlCommandValidationException("Failed to serialize control repository JSON: " + exception.getMessage());
        }
    }

    private <T> T readJson(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException exception) {
            throw new ControlCommandValidationException("Failed to read control repository JSON: " + exception.getMessage());
        }
    }
}
