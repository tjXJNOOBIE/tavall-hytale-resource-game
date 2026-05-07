package com.tavall.hytale.resourcegame.liveops.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigValidationException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GlobalGuiRegistry {
    private final ObjectMapper objectMapper;
    private final Map<String, GlobalGuiDefinition> definitionsByKey = new ConcurrentHashMap<>();

    public GlobalGuiRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }

    public void reload(Collection<GlobalGuiDefinition> definitions) {
        definitionsByKey.clear();
        if (definitions != null) {
            definitions.forEach(this::apply);
        }
    }

    public void apply(GlobalGuiDefinition definition) {
        definitionsByKey.put(definition.guiKey(), definition);
    }

    public Optional<GlobalGuiDefinition> find(String guiKey) {
        return Optional.ofNullable(definitionsByKey.get(guiKey)).filter(GlobalGuiDefinition::enabled);
    }

    public JsonNode layout(String guiKey) {
        return find(guiKey).map(definition -> parse(definition.layoutJson())).orElseGet(MissingNode::getInstance);
    }

    public long getVersion(String guiKey) {
        return find(guiKey).map(GlobalGuiDefinition::version).orElse(0L);
    }

    public List<GlobalGuiDefinition> definitions() {
        return definitionsByKey.values().stream()
                .sorted(Comparator.comparing(GlobalGuiDefinition::guiKey))
                .toList();
    }

    private JsonNode parse(String layoutJson) {
        try {
            return objectMapper.readTree(layoutJson);
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Invalid global GUI layout JSON.", ex);
        }
    }
}
