package org.tavall.control.dependency;

import com.hypixel.hytale.server.core.universe.world.World;
import org.tavall.control.clock.IKingdomClockService;
import org.tavall.control.domain.KingdomClockState;

import java.time.Instant;

/**
 * Test stub for kingdom clock access.
 */
public final class TestKingdomClockService implements IKingdomClockService {
    @Override
    public KingdomClockState snapshot() {
        return new KingdomClockState(Instant.parse("2026-04-09T00:00:00Z"), true, "UTC");
    }

    @Override
    public void applyToWorld(World world) {
    }

    @Override
    public void applyToAllWorlds() {
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }
}

