package org.tavall.control.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.control.api.PlayerDataApi;
import org.tavall.control.api.RankApi;
import org.tavall.control.interaction.ControlPlaneInteractionService;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.transport.ControlPlaneTcpBridgeRequest;
import org.tavall.control.transport.ControlPlaneTcpBridgeRequestType;
import org.tavall.control.runtime.JsonMapperProvider;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class ControlPlaneTcpBridgeServer implements AutoCloseable {
    private static final ObjectMapper OBJECT_MAPPER = new JsonMapperProvider().mapper();

    private final ControlPlaneTcpBridgeConfiguration configuration;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger connectionCounter = new AtomicInteger();

    private volatile ServerSocket serverSocket;
    private volatile Thread acceptThread;

    private ControlPlaneTcpBridgeServer(ControlPlaneTcpBridgeConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    public static ControlPlaneTcpBridgeServer start(ControlPlaneTcpBridgeConfiguration configuration) throws IOException {
        ControlPlaneTcpBridgeServer server = new ControlPlaneTcpBridgeServer(configuration);
        server.start();
        return server;
    }

    public synchronized void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Control bridge is already running.");
        }

        ServerSocket localServerSocket = new ServerSocket();
        try {
            localServerSocket.setReuseAddress(true);
            localServerSocket.bind(new InetSocketAddress(InetAddress.getByName(configuration.host()), configuration.port()));
            serverSocket = localServerSocket;

            acceptThread = new Thread(this::acceptLoop, "tavall-control-bridge-accept");
            acceptThread.setDaemon(true);
            acceptThread.start();

            Log.info("Control bridge listening on " + boundAddress());
        } catch (IOException | RuntimeException exception) {
            running.set(false);
            try {
                localServerSocket.close();
            } catch (IOException ignored) {
                // Ignore cleanup errors during startup failure.
            }
            throw exception;
        }
    }

    public InetSocketAddress boundAddress() {
        ServerSocket localServerSocket = serverSocket;
        if (localServerSocket == null) {
            throw new IllegalStateException("Control bridge has not been started.");
        }
        return (InetSocketAddress) localServerSocket.getLocalSocketAddress();
    }

    public int localPort() {
        return boundAddress().getPort();
    }

    private void acceptLoop() {
        while (running.get()) {
            ServerSocket localServerSocket = serverSocket;
            if (localServerSocket == null) {
                return;
            }
            try {
                Socket clientSocket = localServerSocket.accept();
                Thread connectionThread = new Thread(
                        () -> handleConnection(clientSocket),
                        "tavall-control-bridge-connection-" + connectionCounter.incrementAndGet()
                );
                connectionThread.setDaemon(true);
                connectionThread.start();
            } catch (SocketException exception) {
                if (running.get()) {
                    Log.info("Control bridge accept loop stopped: " + exception.getMessage());
                }
                return;
            } catch (IOException exception) {
                if (running.get()) {
                    Log.info("Control bridge accept loop encountered an IO error: " + exception.getMessage());
                }
                return;
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (
                Socket clientSocket = socket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            String requestJson = reader.readLine();
            ControlPlaneTcpBridgeResponse response = handleRequest(requestJson);
            writer.write(OBJECT_MAPPER.writeValueAsString(response));
            writer.newLine();
            writer.flush();
        } catch (IOException | RuntimeException exception) {
            // The client disconnected or the connection became invalid after the request was accepted.
        }
    }

    private ControlPlaneTcpBridgeResponse handleRequest(String requestJson) {
        if (requestJson == null || requestJson.isBlank()) {
            return ControlPlaneTcpBridgeResponse.error("Invalid control bridge request: empty payload.");
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(requestJson);
            if (root.hasNonNull("requestType")) {
                ControlPlaneTcpBridgeRequest request = OBJECT_MAPPER.treeToValue(root, ControlPlaneTcpBridgeRequest.class);
                return switch (request.requestType()) {
                    case FRONTEND_COMMAND -> handleCommandRequest(request.frontendCommand());
                    case INTERACTION_REQUEST -> handleInteractionRequest(request.interactionRequest());
                    case PLAYER_DATA_REQUEST -> handlePlayerDataRequest(request.playerDataRequest());
                    case RANK_REQUEST -> handleRankRequest(request.rankRequest());
                    case PUNISH_REQUEST -> handlePunishRequest(request.punishRequest());
                };
            }
            FrontendCommandEnvelope envelope = OBJECT_MAPPER.treeToValue(root, FrontendCommandEnvelope.class);
            return handleCommandRequest(envelope);
        } catch (Exception exception) {
            return ControlPlaneTcpBridgeResponse.error("Invalid control bridge request: " + safeMessage(exception));
        }
    }

    private ControlPlaneTcpBridgeResponse handleCommandRequest(FrontendCommandEnvelope envelope) {
        ControlCommandRuntime runtime = DependencyLoaderAccess.findInstance(ControlCommandRuntime.class);
        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());
        return ControlPlaneTcpBridgeResponse.success(result);
    }

    private ControlPlaneTcpBridgeResponse handleInteractionRequest(InteractionRequest request) {
        ControlPlaneInteractionService service = DependencyLoaderAccess.findInstance(ControlPlaneInteractionService.class);
        InteractionResult result = service.handle(request, Instant.now());
        return ControlPlaneTcpBridgeResponse.success(result);
    }

    private ControlPlaneTcpBridgeResponse handlePlayerDataRequest(PlayerDataRequest request) {
        PlayerDataApi api = DependencyLoaderAccess.findInstance(PlayerDataApi.class);
        PlayerDataResponse result = api.inspect(request, Instant.now());
        return ControlPlaneTcpBridgeResponse.success(result);
    }

    private ControlPlaneTcpBridgeResponse handleRankRequest(RankRequest request) {
        RankApi api = DependencyLoaderAccess.findInstance(RankApi.class);
        RankResponse result = api.inspect(request, Instant.now());
        return ControlPlaneTcpBridgeResponse.success(result);
    }

    private ControlPlaneTcpBridgeResponse handlePunishRequest(PunishRequest request) {
        org.tavall.control.api.PunishApi api = DependencyLoaderAccess.findInstance(org.tavall.control.api.PunishApi.class);
        PunishResponse result = api.inspect(request, Instant.now());
        return ControlPlaneTcpBridgeResponse.success(result);
    }

    @Override
    public synchronized void close() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        ServerSocket localServerSocket = serverSocket;
        if (localServerSocket != null) {
            try {
                localServerSocket.close();
            } catch (IOException ignored) {
                // Ignore shutdown errors.
            }
        }

        Thread localAcceptThread = acceptThread;
        if (localAcceptThread != null) {
            try {
                localAcceptThread.join(TimeUnit.SECONDS.toMillis(2));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        Log.info("Control bridge stopped.");
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
