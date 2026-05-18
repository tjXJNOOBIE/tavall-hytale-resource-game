package org.tavall.control.protection;
import org.tavall.control.protection.StructureProtectionHandler;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.protection.IProtectedBlockSystemHandler;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs protected-block ECS listeners after the base universe finishes booting.
 */
public final class ProtectedBlockSystemHandler implements IProtectedBlockSystemHandler, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(ProtectedBlockSystemHandler.class.getName());
    private static final long STARTUP_DELAY_SECONDS = 20L;

    private final StructureProtectionHandler protectionHandler;
    private ScheduledFuture<?> startupTask;
    private boolean registered;

    public ProtectedBlockSystemHandler(StructureProtectionHandler protectionHandler) {
        this.protectionHandler = Objects.requireNonNull(protectionHandler, "protectionHandler");
    }

    @Override
    public synchronized void start() {
        if (registered || startupTask != null) {
            return;
        }
        startupTask = HytaleServer.SCHEDULED_EXECUTOR.schedule(
                this::registerSafely,
                STARTUP_DELAY_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    public synchronized void shutdown() {
        if (startupTask != null) {
            startupTask.cancel(false);
            startupTask = null;
        }
        unregisterSafely();
    }

    private synchronized void registerSafely() {
        startupTask = null;
        if (registered) {
            return;
        }
        try {
            unregisterSafely();
            EntityStore.REGISTRY.registerSystem(new ProtectedBlockBreakSystem(protectionHandler));
            EntityStore.REGISTRY.registerSystem(new ProtectedBlockPlaceSystem(protectionHandler));
            registered = true;
            LOGGER.info("Protected block ECS systems registered.");
        } catch (Throwable throwable) {
            LOGGER.log(Level.WARNING, "Failed to register protected block ECS systems.", throwable);
        }
    }

    private void unregisterSafely() {
        try {
            EntityStore.REGISTRY.unregisterSystem(ProtectedBlockBreakSystem.class);
        } catch (Throwable ignored) {
        }
        try {
            EntityStore.REGISTRY.unregisterSystem(ProtectedBlockPlaceSystem.class);
        } catch (Throwable ignored) {
        }
        registered = false;
    }
}

