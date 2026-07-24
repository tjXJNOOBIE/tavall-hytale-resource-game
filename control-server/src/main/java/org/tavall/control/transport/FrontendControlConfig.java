package org.tavall.control.transport;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.api.minecraft.frontend.IFrontendControlConfig;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public final class FrontendControlConfig implements IFrontendControlConfig, IDependencyInjectableConcrete {
    private final URI controlIngressUri;
    private final String serverId;

    public FrontendControlConfig(URI controlIngressUri, String serverId) {
        this.controlIngressUri = requireTcpUri(controlIngressUri);
        this.serverId = serverId;
    }

    public static FrontendControlConfig fromEnvironment(Map<String, String> environment) {
        return fromEnvironment(environment, URI.create("tcp://127.0.0.1:18081"));
    }

    public static FrontendControlConfig fromEnvironment(Map<String, String> environment, URI defaultControlIngressUri) {
        Map<String, String> safeEnvironment = environment == null ? Map.of() : environment;
        URI safeDefaultControlIngressUri = Objects.requireNonNull(defaultControlIngressUri, "defaultControlIngressUri");
        return new FrontendControlConfig(
                URI.create(firstNonBlank(
                        safeEnvironment.get("RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL"),
                        safeEnvironment.get("RESOURCE_GAME_MINECRAFT_CONTROL_BRIDGE_URL"),
                        safeEnvironment.get("RESOURCE_GAME_CONTROL_INGRESS_URL"),
                        safeEnvironment.get("TAVALL_CONTROL_INGRESS_URL"),
                        safeEnvironment.get("RESOURCE_GAME_CONTROL_BRIDGE_URL"),
                        safeEnvironment.get("TAVALL_CONTROL_BRIDGE_URL"),
                        safeDefaultControlIngressUri.toString()
                )),
                firstNonBlank(
                        safeEnvironment.get("RESOURCE_GAME_MINECRAFT_SERVER_ID"),
                        safeEnvironment.get("RESOURCE_GAME_CONTROL_SERVER_ID"),
                        safeEnvironment.get("TAVALL_CONTROL_SERVER_ID"),
                        "minecraft-single-server"
                )
        );
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalArgumentException("At least one frontend control config value is required.");
    }

    @Override
    public URI controlIngressUri() {
        return controlIngressUri;
    }

    @Override
    public String serverId() {
        return serverId;
    }

    private static URI requireTcpUri(URI controlIngressUri) {
        URI safeControlIngressUri = Objects.requireNonNull(controlIngressUri, "controlIngressUri");
        String scheme = safeControlIngressUri.getScheme();
        if (!"tcp".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Private control ingress must use tcp://host:port, got: " + safeControlIngressUri);
        }
        return safeControlIngressUri;
    }
}
