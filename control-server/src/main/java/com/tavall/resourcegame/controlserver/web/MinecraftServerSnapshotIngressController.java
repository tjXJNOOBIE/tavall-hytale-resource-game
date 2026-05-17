package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshot;
import com.tavall.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshotResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class MinecraftServerSnapshotIngressController implements IControlServerDomain {
    @PostMapping("/api/frontend/minecraft/server-snapshots")
    public ResponseEntity<MinecraftServerRuntimeSnapshotResult> ingestMinecraftServerSnapshot(@RequestBody MinecraftServerRuntimeSnapshot snapshot) {
        MinecraftServerRuntimeSnapshotResult result = getMinecraftServerSnapshotIngressHandler().ingest(snapshot, Instant.now());
        return result.accepted()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/api/frontend/minecraft/server-snapshots/latest")
    public ResponseEntity<MinecraftServerRuntimeSnapshot> latestMinecraftServerSnapshot(@RequestParam(required = false) String serverId) {
        return (serverId == null || serverId.isBlank()
                ? getMinecraftServerSnapshotIngressHandler().latestAny()
                : getMinecraftServerSnapshotIngressHandler().latest(serverId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
