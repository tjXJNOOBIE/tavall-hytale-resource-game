package org.tavall.control.bootstrap;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import org.tavall.control.ResourceGamePlugin;
import org.tavall.control.ResourceGameControlServerModule;
import org.tavall.control.cli.ControlConsoleInputHandler;
import org.tavall.control.transport.ControlPlaneTcpBridgeConfiguration;
import org.tavall.control.transport.ControlPlaneTcpBridgeServer;
import org.tavall.control.bootstrap.ResourceGameDependencyModule;
import org.tavall.control.interactions.OpenFarmsteadInteraction;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.injection.helpers.DependencyInjectorHelper;
import org.tavall.dependency.injection.helpers.interfaces.IDependencyInjectorHelper;
import org.tavall.logging.Log;
import org.tavall.internal.utils.concurrent.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public final class ControlServerBootstrap {
    private final IDependencyInjectorHelper injectorHelper = new DependencyInjectorHelper();
    private static volatile boolean uncaughtHandlerInstalled = false;

    public void setup(ResourceGamePlugin plugin) {
        OpenFarmsteadInteraction.registerCodec();
        try {
            injectorHelper.setupDISystem(new ResourceGameDependencyModule(plugin));
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to initialize ResourceGame DI.", throwable);
        }
        plugin.getLogger().atInfo().log("Kingdom clock initialized. Daytime: %s", plugin.getKingdomClockHandler().snapshot().isDay());
    }

    public void start(ResourceGamePlugin plugin) {
        installUncaughtExceptionHandler(plugin);
        plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, plugin.getPlayerDataHandler()::handlePlayerReady);
        plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, plugin.getPlayerDataHandler()::handlePlayerDisconnect);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, plugin.getPlacementInteractionHandler()::handleInteract);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, plugin.getCastleInteractionHandler()::handleInteract);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, plugin.getResourceNodeInteractionHandler()::handleInteract);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, plugin.getBuildingInteractionHandler()::handleInteract);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, plugin.getWorkerNpcInteractionHandler()::handleInteract);
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, plugin.getCustomEntitySpawnHandler()::handleInteract);
        plugin.getInteriorInstanceHandler().pruneTransientWorlds();
        plugin.getInteriorInstanceHandler().warmInteriorWorld().whenComplete((world, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().atWarning().withCause(throwable).log("Failed to warm interior world during plugin startup.");
                return;
            }
            if (world != null) {
                plugin.getLogger().atInfo().log("Interior world warmed during plugin startup: %s", world.getName());
            }
        });
        plugin.getKingdomClockHandler().start();
        plugin.getCastleProximityPromptHandler().start();
        plugin.getCastleEconomySimulationHandler().start();
        plugin.getResourceNodeVisualPulseHandler().start();
        plugin.getProtectedBlockSystemHandler().start();
        plugin.getVisualVerificationControlHandler().start();

        List<AbstractAsyncCommand> commands = plugin.getDebugCommandHandler().commands();
        for (AbstractAsyncCommand command : commands) {
            plugin.getCommandRegistry().registerCommand(command);
            plugin.getLogger().atInfo().log("Registered command /%s aliases=%s", command.getName(), command.getAliases());
        }
    }

    public void shutdown(ResourceGamePlugin plugin) {
        plugin.getCastleProximityPromptHandler().shutdown();
        plugin.getCastleEconomySimulationHandler().shutdown();
        plugin.getResourceNodeVisualPulseHandler().shutdown();
        plugin.getProtectedBlockSystemHandler().shutdown();
        plugin.getVisualVerificationControlHandler().shutdown();
        plugin.getKingdomClockHandler().shutdown();
        AsyncTask.shutdown();
    }

    public void runConsole(String[] args) {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        ControlPlaneTcpBridgeServer bridgeServer;
        try {
            bridgeServer = ControlPlaneTcpBridgeServer.start(ControlPlaneTcpBridgeConfiguration.fromEnvironment(System.getenv()));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start the control-plane TCP bridge.", exception);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(bridgeServer::close, "tavall-control-bridge-shutdown"));

        ControlConsoleInputHandler inputHandler = new ControlConsoleInputHandler();
        try {
            Log.info("Control console bridge is available on " + bridgeServer.boundAddress());
            if (args.length > 0) {
                System.out.print(inputHandler.executeOneShotCommand(String.join(" ", args)));
                return;
            }
            inputHandler.runInteractiveConsole(new Scanner(System.in), new PrintWriter(System.out, true));
        } finally {
            bridgeServer.close();
            DependencyLoaderAccess.clear();
        }
    }

    private void installUncaughtExceptionHandler(ResourceGamePlugin plugin) {
        if (uncaughtHandlerInstalled) {
            return;
        }
        uncaughtHandlerInstalled = true;
        Thread.UncaughtExceptionHandler existing = Thread.getDefaultUncaughtExceptionHandler();
        Thread.UncaughtExceptionHandler handler = (thread, throwable) -> {
            try {
                plugin.getLogger().atSevere().withCause(throwable).log("Uncaught exception on thread %s", thread.getName());
                System.err.println("[ResourceGamePlugin] Uncaught exception on thread " + thread.getName());
                throwable.printStackTrace(System.err);
            } catch (Throwable ignored) {
            }
            if (existing != null) {
                try {
                    existing.uncaughtException(thread, throwable);
                } catch (Throwable ignored) {
                }
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(handler);
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread == null || thread == Thread.currentThread()) {
                continue;
            }
            String threadName = thread.getName();
            if (threadName == null || (!threadName.startsWith("WorldThread") && !"main".equals(threadName))) {
                continue;
            }
            try {
                thread.setUncaughtExceptionHandler(handler);
            } catch (Throwable ignored) {
            }
        }
    }
}

