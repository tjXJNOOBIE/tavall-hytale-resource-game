package com.tavall.hytale.resourcegame.liveops.config;

import java.util.List;
import java.util.Optional;

public interface LiveConfigRepository {
    Optional<LiveConfigEntry> findByKey(String key, String environment);

    List<LiveConfigEntry> findByEnvironment(String environment);

    void save(LiveConfigEntry entry);
}
