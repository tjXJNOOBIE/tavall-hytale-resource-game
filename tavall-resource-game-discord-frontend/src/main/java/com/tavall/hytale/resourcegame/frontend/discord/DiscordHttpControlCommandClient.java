package com.tavall.hytale.resourcegame.frontend.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendControlCommandClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public final class DiscordHttpControlCommandClient implements FrontendControlCommandClient {
    private final URI ingressUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DiscordHttpControlCommandClient(URI ingressUri) {
        this(
                ingressUri,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                new ObjectMapper()
                        .registerModule(new ParameterNamesModule())
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        );
    }

    DiscordHttpControlCommandClient(URI ingressUri, HttpClient httpClient, ObjectMapper objectMapper) {
        this.ingressUri = Objects.requireNonNull(ingressUri, "ingressUri");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        try {
            String requestBody = objectMapper.writeValueAsString(envelope);
            HttpRequest request = HttpRequest.newBuilder(ingressUri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return FrontendCommandVerificationResult.rejected(
                        envelope,
                        "Control ingress rejected Discord command with HTTP status " + response.statusCode() + "."
                );
            }
            return objectMapper.readValue(response.body(), FrontendCommandVerificationResult.class);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return FrontendCommandVerificationResult.rejected(envelope, "Discord command submission was interrupted.");
        } catch (IOException exception) {
            return FrontendCommandVerificationResult.rejected(envelope, "Discord command submission failed: " + exception.getMessage());
        }
    }
}
