package com.tavall.hytale.resourcegame.liveops.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class LiveConfigRegistry {
    private final ObjectMapper objectMapper;
    private final Map<String, LiveConfigEntry> entriesByKey = new ConcurrentHashMap<>();

    public LiveConfigRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }

    public void reload(Collection<LiveConfigEntry> entries) {
        entriesByKey.clear();
        if (entries != null) {
            entries.forEach(this::apply);
        }
    }

    public void apply(LiveConfigEntry entry) {
        entriesByKey.put(entry.key(), entry);
    }

    public Optional<LiveConfigEntry> find(String key) {
        return Optional.ofNullable(entriesByKey.get(key));
    }

    public boolean isEnabled(String key) {
        return find(key).map(LiveConfigEntry::enabled).orElse(false);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean fallback) {
        return find(key).map(entry -> parseJson(entry.valueJson()).asBoolean(fallback)).orElse(fallback);
    }

    public int getInt(String key, int fallback) {
        return find(key).map(entry -> parseJson(entry.valueJson()).asInt(fallback)).orElse(fallback);
    }

    public double getDouble(String key, double fallback) {
        return find(key).map(entry -> parseJson(entry.valueJson()).asDouble(fallback)).orElse(fallback);
    }

    public String getString(String key, String fallback) {
        return find(key).map(entry -> {
            JsonNode node = parseJson(entry.valueJson());
            return node.isTextual() ? node.asText() : node.toString();
        }).orElse(fallback);
    }

    public JsonNode getJson(String key) {
        return find(key).map(entry -> parseJson(entry.valueJson())).orElseGet(MissingNode::getInstance);
    }

    public long getVersion(String key) {
        return find(key).map(LiveConfigEntry::version).orElse(0L);
    }

    public List<LiveConfigEntry> entries() {
        return entriesByKey.values().stream()
                .sorted(Comparator.comparing(LiveConfigEntry::key))
                .toList();
    }

    private JsonNode parseJson(String valueJson) {
        try {
            return objectMapper.readTree(valueJson);
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Invalid live config JSON value.", ex);
        }
    }
}
