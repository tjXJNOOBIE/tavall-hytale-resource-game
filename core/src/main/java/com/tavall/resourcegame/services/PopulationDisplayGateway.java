package com.tavall.resourcegame.services;

import com.hypixel.hytale.server.core.universe.world.World;
import com.tavall.resourcegame.domain.PopulationSummary;
import com.tavall.resourcegame.interior.InteriorLayout;

import java.util.UUID;

/**
 * Boundary for population display updates.
 */
public interface PopulationDisplayGateway {
    void ensureDisplays(UUID playerId, World world, InteriorLayout layout, PopulationSummary summary);

    void updateDisplays(UUID playerId, PopulationSummary summary);

    void clearDisplays(UUID playerId);
}
