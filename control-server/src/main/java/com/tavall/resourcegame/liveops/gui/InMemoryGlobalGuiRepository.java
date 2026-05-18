package org.tavall.control.liveops.gui;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryGlobalGuiRepository implements GlobalGuiRepository {
    private final Map<String, GlobalGuiDefinition> definitionsByKey = new ConcurrentHashMap<>();

    @Override
    public Optional<GlobalGuiDefinition> findByKey(String guiKey) {
        return Optional.ofNullable(definitionsByKey.get(guiKey));
    }

    @Override
    public List<GlobalGuiDefinition> findEnabled() {
        return definitionsByKey.values().stream()
                .filter(GlobalGuiDefinition::enabled)
                .sorted(Comparator.comparing(GlobalGuiDefinition::guiKey))
                .toList();
    }

    @Override
    public void save(GlobalGuiDefinition definition) {
        definitionsByKey.put(definition.guiKey(), definition);
    }
}
