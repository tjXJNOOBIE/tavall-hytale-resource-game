package com.tavall.resourcegame.controlserver.cli;

import com.tavall.resourcegame.controlserver.bootstrap.ControlServerBootstrap;

public final class ControlConsoleApplication {
    private static final ControlServerBootstrap BOOTSTRAP = new ControlServerBootstrap();

    private ControlConsoleApplication() {
    }

    public static void main(String[] args) {
        BOOTSTRAP.runConsole(args);
    }
}
