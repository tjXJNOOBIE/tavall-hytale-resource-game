package org.tavall.control.services;

import com.hypixel.hytale.server.core.universe.world.World;
import org.tavall.control.domain.PopulationSummary;
import org.tavall.control.interior.InteriorLayout;

import java.util.UUID;

/**
 * Boundary for population display updates.
 */
public interface PopulationDisplayGateway {
    void ensureDisplays(UUID playerId, World world, InteriorLayout layout, PopulationSummary summary);

    void updateDisplays(UUID playerId, PopulationSummary summary);

    void clearDisplays(UUID playerId);
}
