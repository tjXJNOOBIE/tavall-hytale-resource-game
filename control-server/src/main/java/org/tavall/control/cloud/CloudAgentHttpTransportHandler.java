package org.tavall.control.cloud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public final class CloudAgentHttpTransportHandler implements ICloudAgentTransportHandler, CloudAgentDomain, IDependencyInjectableConcrete {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public boolean sendHeartbeat(CloudAgentHeartbeatPayload heartbeat) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(jsonPost(endpoint("/heartbeat"), heartbeat), HttpResponse.BodyHandlers.ofString());
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    @Override
    public List<CloudCommand> pollCommands() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(endpoint("/commands"))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 204 || response.body() == null || response.body().isBlank()) {
            return List.of();
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Cloud agent command poll failed with status " + response.statusCode());
        }
        return getCloudAgentObjectMapper().readValue(response.body(), new TypeReference<List<CloudCommand>>() {
        });
    }

    @Override
    public boolean reportResult(CloudCommandResult result) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(jsonPost(endpoint("/results"), result), HttpResponse.BodyHandlers.ofString());
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private HttpRequest jsonPost(URI uri, Object payload) throws IOException {
        return HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(getCloudAgentObjectMapper().writeValueAsString(payload)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
    }

    private URI endpoint(String suffix) {
        String base = getCloudAgentRuntimeConfig().controlPlaneUri().toString();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return URI.create(base + "/api/cloud/agent/" + getCloudAgentRuntimeConfig().nodeId() + suffix);
    }
}
