package org.tavall.control.web;

import org.tavall.control.cloud.CloudAgentHeartbeatPayload;
import org.tavall.control.cloud.CloudCommand;
import org.tavall.control.cloud.CloudCommandResult;
import org.tavall.control.cloud.CloudControlDomain;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public final class CloudAgentIngressController implements CloudControlDomain {
    @PostMapping("/api/cloud/agent/{nodeId}/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(
            @PathVariable UUID nodeId,
            @RequestBody CloudAgentHeartbeatPayload heartbeat
    ) {
        if (!nodeId.equals(heartbeat.nodeId())) {
            return ResponseEntity.badRequest().body(Map.of("accepted", false, "message", "Heartbeat node id mismatch."));
        }
        boolean accepted = getNodeHeartbeatHandler().heartbeat(nodeId, heartbeat.heartbeatAt() == null ? Instant.now() : heartbeat.heartbeatAt());
        return accepted
                ? ResponseEntity.ok(Map.of("accepted", true))
                : ResponseEntity.status(404).body(Map.of("accepted", false, "message", "Node is not registered."));
    }

    @GetMapping("/api/cloud/agent/{nodeId}/commands")
    public ResponseEntity<List<CloudCommand>> commands(
            @PathVariable UUID nodeId,
            @RequestParam(defaultValue = "10") int maxCommands
    ) {
        return ResponseEntity.ok(getAgentCommandPollHandler().poll(nodeId, maxCommands, Instant.now()));
    }

    @PostMapping("/api/cloud/agent/{nodeId}/results")
    public ResponseEntity<Map<String, Object>> result(
            @PathVariable UUID nodeId,
            @RequestBody CloudCommandResult result
    ) {
        if (!nodeId.equals(result.nodeId())) {
            return ResponseEntity.badRequest().body(Map.of("accepted", false, "message", "Result node id mismatch."));
        }
        boolean accepted = getAgentCommandResultReportHandler().report(result);
        return accepted
                ? ResponseEntity.ok(Map.of("accepted", true))
                : ResponseEntity.status(404).body(Map.of("accepted", false, "message", "Command is not registered."));
    }
}
