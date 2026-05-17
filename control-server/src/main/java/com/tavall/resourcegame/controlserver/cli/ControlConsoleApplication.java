package com.tavall.resourcegame.controlserver.cli;

import com.tavall.resourcegame.controlserver.ResourceGameControlServerModule;
import com.tavall.resourcegame.controlserver.transport.ControlPlaneTcpBridgeConfiguration;
import com.tavall.resourcegame.controlserver.transport.ControlPlaneTcpBridgeServer;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public final class ControlConsoleApplication {
    private ControlConsoleApplication() {
    }

    public static void main(String[] args) {
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
}
