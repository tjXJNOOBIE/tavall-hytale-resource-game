package org.tavall.control.resource;
import org.tavall.control.player.PlayerSessionStore;
import org.tavall.control.player.PlayerSession;

import com.hypixel.hytale.server.core.HytaleServer;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.player.IPlayerSessionStore;
import org.tavall.control.resource.IResourceNodeVisualPulseHandler;
import org.tavall.control.resource.IResourceNodeVisualHandler;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Refreshes node route visuals frequently enough to show convoy movement.
 */
public final class ResourceNodeVisualPulseHandler implements IResourceNodeVisualPulseHandler, IDependencyInjectableConcrete {
    private static final long PULSE_INTERVAL_SECONDS = 3L;

    private final IPlayerSessionStore sessionStore;
    private final IResourceNodeVisualHandler resourceNodeVisualHandler;
    private ScheduledFuture<?> pulseTask;

    public ResourceNodeVisualPulseHandler(
            IPlayerSessionStore sessionStore,
            IResourceNodeVisualHandler resourceNodeVisualHandler
    ) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.resourceNodeVisualHandler = Objects.requireNonNull(resourceNodeVisualHandler, "resourceNodeVisualHandler");
    }

    @Override
    public void start() {
        if (pulseTask != null && !pulseTask.isCancelled()) {
            return;
        }
        pulseTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                this::runPulse,
                PULSE_INTERVAL_SECONDS,
                PULSE_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void shutdown() {
        if (pulseTask != null) {
            pulseTask.cancel(false);
            pulseTask = null;
        }
    }

    public void runPulse() {
        for (PlayerSession session : sessionStore.snapshot()) {
            if (session.gameState() == null) {
                continue;
            }
            resourceNodeVisualHandler.refreshNodes(session.playerId(), session.gameState());
        }
    }
}

