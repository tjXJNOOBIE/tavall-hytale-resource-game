package org.tavall.minecraft.permissions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.dependency.IDependencyInjectableConcrete;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public final class VelocityRankControlBridgeClient implements IRankControlBridgeClient, IDependencyInjectableConcrete {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final URI controlIngressUri;

    public VelocityRankControlBridgeClient() {
        this(resolveControlIngressUri(System.getenv()));
    }

    public VelocityRankControlBridgeClient(URI controlIngressUri) {
        this.controlIngressUri = requireTcpUri(Objects.requireNonNull(controlIngressUri, "controlIngressUri"));
    }

    public static VelocityRankControlBridgeClient fromEnvironment(Map<String, String> environment) {
        return new VelocityRankControlBridgeClient(resolveControlIngressUri(environment));
    }

    @Override
    public RankResponse submitRankRequest(RankRequest request) {
        Objects.requireNonNull(request, "request");
        String host = controlIngressUri.getHost();
        int port = controlIngressUri.getPort();
        if (host == null || host.isBlank()) {
            return RankResponse.unavailable(request.requestId(), "Control bridge URI is missing a host.");
        }
        if (port < 0) {
            return RankResponse.unavailable(request.requestId(), "Control bridge URI is missing a port.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.toIntExact(CONNECT_TIMEOUT.toMillis()));
            socket.setSoTimeout(Math.toIntExact(READ_TIMEOUT.toMillis()));
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                writer.write(OBJECT_MAPPER.writeValueAsString(new RankBridgeRequest("RANK_REQUEST", request)));
                writer.newLine();
                writer.flush();

                String responseJson = reader.readLine();
                if (responseJson == null) {
                    return RankResponse.unavailable(request.requestId(), "Control bridge closed the connection without returning a response.");
                }

                RankBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, RankBridgeResponse.class);
                if (response.rankResponse() != null) {
                    return response.rankResponse();
                }
                return RankResponse.unavailable(request.requestId(), safeMessage(response.errorMessage()));
            }
        } catch (SocketTimeoutException exception) {
            return RankResponse.unavailable(request.requestId(), "Control bridge timed out: " + safeMessage(exception));
        } catch (IOException | RuntimeException exception) {
            return RankResponse.unavailable(request.requestId(), "Control bridge unavailable: " + safeMessage(exception));
        }
    }

    private static URI resolveControlIngressUri(Map<String, String> environment) {
        Map<String, String> safeEnvironment = environment == null ? Map.of() : environment;
        return URI.create(firstNonBlank(
                safeEnvironment.get("RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL"),
                safeEnvironment.get("RESOURCE_GAME_MINECRAFT_CONTROL_BRIDGE_URL"),
                safeEnvironment.get("RESOURCE_GAME_CONTROL_INGRESS_URL"),
                safeEnvironment.get("TAVALL_CONTROL_INGRESS_URL"),
                safeEnvironment.get("RESOURCE_GAME_CONTROL_BRIDGE_URL"),
                safeEnvironment.get("TAVALL_CONTROL_BRIDGE_URL"),
                "tcp://127.0.0.1:18081"
        ));
    }

    private static URI requireTcpUri(URI controlIngressUri) {
        String scheme = controlIngressUri.getScheme();
        if (!"tcp".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Control ingress must use tcp://host:port, got: " + controlIngressUri);
        }
        return controlIngressUri;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalArgumentException("At least one control bridge value is required.");
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }

    private String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Control bridge returned an empty error message.";
        }
        return message;
    }

    private record RankBridgeRequest(String requestType, RankRequest rankRequest) {
    }

    private record RankBridgeResponse(RankResponse rankResponse, String errorMessage) {
    }
}
