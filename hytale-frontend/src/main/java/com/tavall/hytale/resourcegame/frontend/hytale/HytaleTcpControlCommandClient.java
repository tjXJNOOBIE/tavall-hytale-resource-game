package com.tavall.hytale.resourcegame.frontend.hytale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public final class HytaleTcpControlCommandClient implements IHytaleControlCommandClient, IHytaleFrontendDomain, IDependencyInjectableConcrete {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        String host = getHytaleFrontendConfig().controlIngressUri().getHost();
        int port = getHytaleFrontendConfig().controlIngressUri().getPort();
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
                    return FrontendCommandVerificationResult.rejected(envelope, "Control bridge closed the connection without returning a response.");
                }

                var root = OBJECT_MAPPER.readTree(responseJson);
                if (root.hasNonNull("verificationResult")) {
                    return OBJECT_MAPPER.treeToValue(root.get("verificationResult"), FrontendCommandVerificationResult.class);
                }
                if (root.hasNonNull("errorMessage")) {
                    return FrontendCommandVerificationResult.rejected(envelope, safeMessage(root.get("errorMessage").asText()));
                }
                return FrontendCommandVerificationResult.rejected(envelope, "Control bridge returned an empty response.");
            }
        } catch (SocketTimeoutException exception) {
            return FrontendCommandVerificationResult.rejected(envelope, "Control bridge timed out: " + safeMessage(exception));
        } catch (IOException | RuntimeException exception) {
            return FrontendCommandVerificationResult.rejected(envelope, "Control bridge unavailable: " + safeMessage(exception));
        }
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
}
