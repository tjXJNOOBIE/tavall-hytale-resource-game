package com.tavall.hytale.resourcegame.liveops.gui;

import java.util.List;
import java.util.Optional;

public interface GlobalGuiRepository {
    Optional<GlobalGuiDefinition> findByKey(String guiKey);

    List<GlobalGuiDefinition> findEnabled();

    void save(GlobalGuiDefinition definition);
}
