package org.tavall.control.cli;

import org.tavall.control.bootstrap.ControlServerBootstrap;

public final class ControlConsoleApplication {
    private static final ControlServerBootstrap BOOTSTRAP = new ControlServerBootstrap();

    private ControlConsoleApplication() {
    }

    public static void main(String[] args) {
        BOOTSTRAP.runConsole(args);
    }
}
