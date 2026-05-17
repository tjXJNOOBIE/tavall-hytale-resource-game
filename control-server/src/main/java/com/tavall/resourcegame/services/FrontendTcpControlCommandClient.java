package com.tavall.resourcegame.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.dependency.composition.domains.IResourceGameDomain;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionRequest;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.player.PlayerDataRequest;
import com.tavall.resourcegame.api.internal.player.PlayerDataResponse;
import com.tavall.resourcegame.api.internal.permissions.PunishRequest;
import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.api.internal.permissions.RankRequest;
import com.tavall.resourcegame.api.internal.permissions.RankResponse;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

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
import java.util.Objects;

public final class FrontendTcpControlCommandClient implements IFrontendControlCommandClient, IResourceGameDomain, IDependencyInjectableConcrete {
    private static final ObjectMapper OBJECT_MAPPER = new JsonMapperProvider().mapper();
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        URI controlBridgeUri = getFrontendControlConfig().controlIngressUri();
        String host = controlBridgeUri.getHost();
        int port = controlBridgeUri.getPort();
        if (host == null || host.isBlank()) {
            return FrontendCommandVerificationResult.rejected(envelope, "Control bridge URI is missing a host.");
        }
        if (port < 0) {
            return FrontendCommandVerificationResult.rejected(envelope, "Control bridge URI is missing a port.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.toIntExact(CONNECT_TIMEOUT.toMillis()));
            socket.setSoTimeout(Math.toIntExact(READ_TIMEOUT.toMillis()));
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                writer.write(OBJECT_MAPPER.writeValueAsString(envelope));
                writer.newLine();
                writer.flush();

                String responseJson = reader.readLine();
                if (responseJson == null) {
                    return FrontendCommandVerificationResult.rejected(
                            envelope,
                            "Control bridge closed the connection without returning a response."
                    );
                }

                FrontendTcpControlBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, FrontendTcpControlBridgeResponse.class);
                if (response.verificationResult() != null) {
                    return response.verificationResult();
                }
                return FrontendCommandVerificationResult.rejected(envelope, safeMessage(response.errorMessage()));
            }
        } catch (SocketTimeoutException exception) {
            return FrontendCommandVerificationResult.rejected(envelope, "Control bridge timed out: " + safeMessage(exception));
        } catch (IOException | RuntimeException exception) {
            return FrontendCommandVerificationResult.rejected(envelope, "Control bridge unavailable: " + safeMessage(exception));
        }
    }

    @Override
    public InteractionResult submitInteraction(InteractionRequest request) {
        Objects.requireNonNull(request, "request");
        URI controlBridgeUri = getFrontendControlConfig().controlIngressUri();
        String host = controlBridgeUri.getHost();
        int port = controlBridgeUri.getPort();
        if (host == null || host.isBlank()) {
            return backendUnavailable(request, "Control bridge URI is missing a host.");
        }
        if (port < 0) {
            return backendUnavailable(request, "Control bridge URI is missing a port.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.toIntExact(CONNECT_TIMEOUT.toMillis()));
            socket.setSoTimeout(Math.toIntExact(READ_TIMEOUT.toMillis()));
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                writer.write(OBJECT_MAPPER.writeValueAsString(ControlPlaneTcpBridgeRequest.interaction(request)));
                writer.newLine();
                writer.flush();

                String responseJson = reader.readLine();
                if (responseJson == null) {
                    return backendUnavailable(request, "Control bridge closed the connection without returning a response.");
                }

                FrontendTcpControlBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, FrontendTcpControlBridgeResponse.class);
                if (response.interactionResult() != null) {
                    return response.interactionResult();
                }
                return backendUnavailable(request, safeMessage(response.errorMessage()));
            }
        } catch (SocketTimeoutException exception) {
            return backendUnavailable(request, "Control bridge timed out: " + safeMessage(exception));
        } catch (IOException | RuntimeException exception) {
            return backendUnavailable(request, "Control bridge unavailable: " + safeMessage(exception));
        }
    }

    @Override
    public PlayerDataResponse fetchPlayerData(PlayerDataRequest request) {
        Objects.requireNonNull(request, "request");
        URI controlBridgeUri = getFrontendControlConfig().controlIngressUri();
        String host = controlBridgeUri.getHost();
        int port = controlBridgeUri.getPort();
        if (host == null || host.isBlank()) {
            return PlayerDataResponse.unavailable(request, "Control bridge URI is missing a host.");
        }
        if (port < 0) {
            return PlayerDataResponse.unavailable(request, "Control bridge URI is missing a port.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.toIntExact(CONNECT_TIMEOUT.toMillis()));
            socket.setSoTimeout(Math.toIntExact(READ_TIMEOUT.toMillis()));
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                writer.write(OBJECT_MAPPER.writeValueAsString(ControlPlaneTcpBridgeRequest.playerData(request)));
                writer.newLine();
                writer.flush();

                String responseJson = reader.readLine();
                if (responseJson == null) {
                    return PlayerDataResponse.unavailable(request, "Control bridge closed the connection without returning a response.");
                }

                FrontendTcpControlBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, FrontendTcpControlBridgeResponse.class);
                if (response.playerDataResponse() != null) {
                    return response.playerDataResponse();
                }
                return PlayerDataResponse.unavailable(request, safeMessage(response.errorMessage()));
            }
        } catch (SocketTimeoutException exception) {
            return PlayerDataResponse.unavailable(request, "Control bridge timed out: " + safeMessage(exception));
        } catch (IOException | RuntimeException exception) {
            return PlayerDataResponse.unavailable(request, "Control bridge unavailable: " + safeMessage(exception));
        }
    }

    @Override
    public RankResponse submitRankRequest(RankRequest request) {
        Objects.requireNonNull(request, "request");
        URI controlBridgeUri = getFrontendControlConfig().controlIngressUri();
        String host = controlBridgeUri.getHost();
        int port = controlBridgeUri.getPort();
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
                writer.write(OBJECT_MAPPER.writeValueAsString(ControlPlaneTcpBridgeRequest.rank(request)));
                writer.newLine();
                writer.flush();

                String responseJson = reader.readLine();
                if (responseJson == null) {
                    return RankResponse.unavailable(request.requestId(), "Control bridge closed the connection without returning a response.");
                }

                FrontendTcpControlBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, FrontendTcpControlBridgeResponse.class);
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

    @Override
    public PunishResponse submitPunishRequest(PunishRequest request) {
        Objects.requireNonNull(request, "request");
        URI controlBridgeUri = getFrontendControlConfig().controlIngressUri();
        String host = controlBridgeUri.getHost();
        int port = controlBridgeUri.getPort();
        if (host == null || host.isBlank()) {
            return PunishResponse.unavailable(request.requestId(), "Control bridge URI is missing a host.");
        }
        if (port < 0) {
            return PunishResponse.unavailable(request.requestId(), "Control bridge URI is missing a port.");
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.toIntExact(CONNECT_TIMEOUT.toMillis()));
            socket.setSoTimeout(Math.toIntExact(READ_TIMEOUT.toMillis()));
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                writer.write(OBJECT_MAPPER.writeValueAsString(ControlPlaneTcpBridgeRequest.punish(request)));
                writer.newLine();
                writer.flush();

                String responseJson = reader.readLine();
                if (responseJson == null) {
                    return PunishResponse.unavailable(request.requestId(), "Control bridge closed the connection without returning a response.");
                }

                FrontendTcpControlBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, FrontendTcpControlBridgeResponse.class);
                if (response.punishResponse() != null) {
                    return response.punishResponse();
                }
                return PunishResponse.unavailable(request.requestId(), safeMessage(response.errorMessage()));
            }
        } catch (SocketTimeoutException exception) {
            return PunishResponse.unavailable(request.requestId(), "Control bridge timed out: " + safeMessage(exception));
        } catch (IOException | RuntimeException exception) {
            return PunishResponse.unavailable(request.requestId(), "Control bridge unavailable: " + safeMessage(exception));
        }
    }

    private InteractionResult backendUnavailable(InteractionRequest request, String message) {
        return new InteractionResult(
                request.requestId(),
                com.tavall.resourcegame.api.internal.interaction.InteractionResultType.BACKEND_UNAVAILABLE,
                false,
                message,
                null,
                message,
                java.util.Map.of(
                        "targetType", request.targetType().name(),
                        "targetId", request.targetId()
                )
        );
    }

    private String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Control bridge returned an empty error message.";
        }
        return message;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
