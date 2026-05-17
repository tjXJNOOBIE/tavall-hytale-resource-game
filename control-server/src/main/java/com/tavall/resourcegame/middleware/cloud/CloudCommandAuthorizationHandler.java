package com.tavall.resourcegame.middleware.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.authority.AuthorizationResult;
import com.tavall.resourcegame.middleware.authority.AuthorityScopeType;
import com.tavall.resourcegame.middleware.authority.ControlCommandRequest;
import com.tavall.resourcegame.middleware.authority.ControlPrincipalType;
import com.tavall.resourcegame.middleware.authority.ResourceTarget;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class CloudCommandAuthorizationHandler implements ICloudCommandAuthorizationHandler, ICloudControlDomain, IDependencyInjectableConcrete {
    /**
     * Cloud commands are node-agent commands, so the authorization target includes the node scope first.
     */
    public AuthorizationResult authorize(UUID nodeId, CloudCommandType commandType, String payloadJson, UUID requestedBy, UUID correlationId, Instant now) {
        ControlCommandRequest request = new ControlCommandRequest(
                correlationId == null ? UUID.randomUUID() : correlationId,
                requestedBy,
                principalType(payloadJson),
                authorityCommandType(commandType),
                target(nodeId, payloadJson),
                payloadJson,
                metadataValue(payloadJson, "reason"),
                now.toEpochMilli(),
                flag(payloadJson, "approvalPresent"),
                flag(payloadJson, "breakGlassActive")
        );
        return getControlAuthorizationHandler().authorize(request);
    }

    private ResourceTarget target(UUID nodeId, String payloadJson) {
        JsonNode payload = payload(payloadJson);
        return new ResourceTarget(
                AuthorityScopeType.NODE,
                nodeId.toString(),
                Map.of(
                        AuthorityScopeType.NODE, nodeId.toString(),
                        AuthorityScopeType.WORKLOAD, metadataValue(payload, "workloadId"),
                        AuthorityScopeType.ENVIRONMENT, metadataValueOrDefault(payload, "environment", "dev")
                )
        );
    }

    private com.tavall.resourcegame.middleware.authority.CloudCommandType authorityCommandType(CloudCommandType commandType) {
        return switch (commandType) {
            case INSTALL_WORKLOAD -> com.tavall.resourcegame.middleware.authority.CloudCommandType.INSTALL_WORKLOAD;
            case START_WORKLOAD -> com.tavall.resourcegame.middleware.authority.CloudCommandType.START_WORKLOAD;
            case STOP_WORKLOAD -> com.tavall.resourcegame.middleware.authority.CloudCommandType.STOP_WORKLOAD;
            case RESTART_WORKLOAD -> com.tavall.resourcegame.middleware.authority.CloudCommandType.RESTART_WORKLOAD;
            case DELETE_WORKLOAD -> com.tavall.resourcegame.middleware.authority.CloudCommandType.DELETE_WORKLOAD;
            case OPEN_PORT -> com.tavall.resourcegame.middleware.authority.CloudCommandType.OPEN_PORT;
            case CLOSE_PORT -> com.tavall.resourcegame.middleware.authority.CloudCommandType.CLOSE_PORT;
            case APPLY_FIREWALL_RULES -> com.tavall.resourcegame.middleware.authority.CloudCommandType.APPLY_FIREWALL_RULES;
            case APPLY_PROXY_CONFIG -> com.tavall.resourcegame.middleware.authority.CloudCommandType.APPLY_PROXY_CONFIG;
            case RUN_HEALTH_CHECK -> com.tavall.resourcegame.middleware.authority.CloudCommandType.RUN_HEALTH_CHECK;
            case COLLECT_LOGS -> com.tavall.resourcegame.middleware.authority.CloudCommandType.COLLECT_LOGS;
            case RUN_BACKUP -> com.tavall.resourcegame.middleware.authority.CloudCommandType.RUN_BACKUP;
            case RESTORE_BACKUP -> com.tavall.resourcegame.middleware.authority.CloudCommandType.RESTORE_BACKUP;
            case UPDATE_AGENT -> com.tavall.resourcegame.middleware.authority.CloudCommandType.UPDATE_AGENT;
            case DRAIN_NODE -> com.tavall.resourcegame.middleware.authority.CloudCommandType.DRAIN_NODE;
            case CANCEL_COMMAND -> com.tavall.resourcegame.middleware.authority.CloudCommandType.CANCEL_COMMAND;
        };
    }

    private ControlPrincipalType principalType(String payloadJson) {
        String value = metadataValue(payload(payloadJson), "principalType");
        if (value.isBlank()) {
            return ControlPrincipalType.SERVICE_ACCOUNT;
        }
        return ControlPrincipalType.valueOf(value);
    }

    private boolean flag(String payloadJson, String key) {
        String value = metadataValue(payload(payloadJson), key);
        return "true".equalsIgnoreCase(value);
    }

    private String metadataValueOrDefault(JsonNode payload, String key, String fallback) {
        String value = metadataValue(payload, key);
        return value.isBlank() ? fallback : value;
    }

    private String metadataValue(String payloadJson, String key) {
        return metadataValue(payload(payloadJson), key);
    }

    private String metadataValue(JsonNode payload, String key) {
        if (payload == null) {
            return "";
        }
        JsonNode value = payload.get(key);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText("");
    }

    private JsonNode payload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return getCloudControlObjectMapper().createObjectNode();
        }
        try {
            return getCloudControlObjectMapper().readTree(payloadJson);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cloud command payload must be valid JSON.", exception);
        }
    }
}
