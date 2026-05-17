package com.tavall.resourcegame.liveops.config;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryLiveConfigRepository implements LiveConfigRepository {
    private final Map<String, LiveConfigEntry> entriesByEnvironmentAndKey = new ConcurrentHashMap<>();

    @Override
    public Optional<LiveConfigEntry> findByKey(String key, String environment) {
        return Optional.ofNullable(entriesByEnvironmentAndKey.get(id(key, environment)));
    }

    @Override
    public List<LiveConfigEntry> findByEnvironment(String environment) {
        return entriesByEnvironmentAndKey.values().stream()
                .filter(entry -> entry.environment().equals(environment))
                .sorted(Comparator.comparing(LiveConfigEntry::key))
                .toList();
    }

    @Override
    public void save(LiveConfigEntry entry) {
        entriesByEnvironmentAndKey.put(id(entry.key(), entry.environment()), entry);
    }

    private String id(String key, String environment) {
        return (environment == null || environment.isBlank() ? "local" : environment) + ":" + key;
    }
}
