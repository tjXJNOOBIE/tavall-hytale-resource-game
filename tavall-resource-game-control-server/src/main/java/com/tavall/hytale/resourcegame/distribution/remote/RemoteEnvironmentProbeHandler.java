package com.tavall.hytale.resourcegame.distribution.remote;

import java.net.InetAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RemoteEnvironmentProbeHandler {
    private final RemoteCommandHandler commandHandler;

    public RemoteEnvironmentProbeHandler(RemoteCommandHandler commandHandler) {
        this.commandHandler = Objects.requireNonNull(commandHandler, "commandHandler");
    }

    public RemoteEnvironmentSnapshot detectLocalEnvironment() {
        Instant now = Instant.now();
        return new RemoteEnvironmentSnapshot(
                "local",
                localHostname(),
                System.getProperty("os.name", ""),
                System.getProperty("java.version", ""),
                System.getProperty("user.name", ""),
                true,
                now,
                Map.of(
                        "processId", Long.toString(ProcessHandle.current().pid()),
                        "javaVendor", System.getProperty("java.vendor", "")
                )
        );
    }

    public RemoteEnvironmentSnapshot probeRemoteEnvironment(RemoteTarget target) {
        if (target.localTarget()) {
            return detectLocalEnvironment();
        }
        Instant now = Instant.now();
        RemoteCommandResult hostname = commandHandler.runRemoteCommand(target, RemoteCommand.safe("hostname"));
        RemoteCommandResult user = commandHandler.runRemoteCommand(target, RemoteCommand.safe("whoami"));
        RemoteCommandResult os = commandHandler.runRemoteCommand(target, RemoteCommand.safe("uname", "-a"));
        RemoteCommandResult java = commandHandler.runRemoteCommand(target, RemoteCommand.safe("java", "-version"));
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("hostnameExitCode", Integer.toString(hostname.exitCode()));
        metadata.put("userExitCode", Integer.toString(user.exitCode()));
        metadata.put("osExitCode", Integer.toString(os.exitCode()));
        metadata.put("javaExitCode", Integer.toString(java.exitCode()));
        boolean reachable = hostname.successful() || user.successful() || os.successful();
        return new RemoteEnvironmentSnapshot(
                target.targetId(),
                firstLine(hostname.stdout()),
                firstLine(os.stdout()),
                firstLine(java.stderr().isBlank() ? java.stdout() : java.stderr()),
                firstLine(user.stdout()),
                reachable,
                now,
                metadata
        );
    }

    private String localHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "localhost";
        }
    }

    private String firstLine(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.lines().findFirst().orElse("").trim();
    }
}
