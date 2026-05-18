package org.tavall.control.transport;

import java.util.Map;
import java.util.Objects;

public record ControlPlaneTcpBridgeConfiguration(String host, int port) {
    public ControlPlaneTcpBridgeConfiguration {
        Objects.requireNonNull(host, "host");
        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
    }

    public static ControlPlaneTcpBridgeConfiguration fromEnvironment(Map<String, String> environment) {
        Map<String, String> safeEnvironment = environment == null ? Map.of() : environment;
        return new ControlPlaneTcpBridgeConfiguration(
                firstNonBlank(
                        safeEnvironment.get("TAVALL_CONTROL_BRIDGE_HOST"),
                        safeEnvironment.get("RESOURCE_GAME_CONTROL_BRIDGE_HOST"),
                        "127.0.0.1"
                ),
                parsePort(firstNonBlank(
                        safeEnvironment.get("TAVALL_CONTROL_BRIDGE_PORT"),
                        safeEnvironment.get("RESOURCE_GAME_CONTROL_BRIDGE_PORT"),
                        "18081"
                ))
        );
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalArgumentException("At least one bridge configuration value is required.");
    }

    private static int parsePort(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid control bridge port: " + value, exception);
        }
    }
}
