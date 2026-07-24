package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.JsonNode;
import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.authority.AuthorizationResult;
import org.tavall.control.authority.AuthorityScopeType;
import org.tavall.control.authority.ControlCommandRequest;
import org.tavall.control.authority.ControlPrincipalType;
import org.tavall.control.authority.ResourceTarget;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class CloudCommandAuthorizationHandler implements ICloudCommandAuthorizationHandler, CloudControlDomain, IDependencyInjectableConcrete {
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

    private org.tavall.control.authority.CloudCommandType authorityCommandType(CloudCommandType commandType) {
        return switch (commandType) {
            case INSTALL_WORKLOAD -> org.tavall.control.authority.CloudCommandType.INSTALL_WORKLOAD;
            case START_WORKLOAD -> org.tavall.control.authority.CloudCommandType.START_WORKLOAD;
            case STOP_WORKLOAD -> org.tavall.control.authority.CloudCommandType.STOP_WORKLOAD;
            case RESTART_WORKLOAD -> org.tavall.control.authority.CloudCommandType.RESTART_WORKLOAD;
            case DELETE_WORKLOAD -> org.tavall.control.authority.CloudCommandType.DELETE_WORKLOAD;
            case OPEN_PORT -> org.tavall.control.authority.CloudCommandType.OPEN_PORT;
            case CLOSE_PORT -> org.tavall.control.authority.CloudCommandType.CLOSE_PORT;
            case APPLY_FIREWALL_RULES -> org.tavall.control.authority.CloudCommandType.APPLY_FIREWALL_RULES;
            case APPLY_PROXY_CONFIG -> org.tavall.control.authority.CloudCommandType.APPLY_PROXY_CONFIG;
            case RUN_HEALTH_CHECK -> org.tavall.control.authority.CloudCommandType.RUN_HEALTH_CHECK;
            case COLLECT_LOGS -> org.tavall.control.authority.CloudCommandType.COLLECT_LOGS;
            case RUN_BACKUP -> org.tavall.control.authority.CloudCommandType.RUN_BACKUP;
            case RESTORE_BACKUP -> org.tavall.control.authority.CloudCommandType.RESTORE_BACKUP;
            case UPDATE_AGENT -> org.tavall.control.authority.CloudCommandType.UPDATE_AGENT;
            case DRAIN_NODE -> org.tavall.control.authority.CloudCommandType.DRAIN_NODE;
            case CANCEL_COMMAND -> org.tavall.control.authority.CloudCommandType.CANCEL_COMMAND;
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
