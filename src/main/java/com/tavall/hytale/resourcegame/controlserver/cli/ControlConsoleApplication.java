package com.tavall.hytale.resourcegame.controlserver.cli;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import com.tavall.hytale.resourcegame.middleware.control.LocalProcessControlSurfaceLaunchHandler;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Scanner;

public final class ControlConsoleApplication {
    private ControlConsoleApplication() {
    }

    public static void main(String[] args) {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime(new LocalProcessControlSurfaceLaunchHandler());
        ControlConsoleInputHandler inputHandler = new ControlConsoleInputHandler(runtime, new ControlConsoleResultRenderer(), ControlOperator.localOwner(Instant.now()));
        if (args.length > 0) {
            System.out.print(inputHandler.executeOneShotCommand(String.join(" ", args)));
            return;
        }
        inputHandler.runInteractiveConsole(new Scanner(System.in), new PrintWriter(System.out, true));
    }
}
