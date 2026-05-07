package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshot;
import com.tavall.hytale.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshotResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class MinecraftServerSnapshotIngressController {
    private final MinecraftServerSnapshotIngressHandler snapshotIngressHandler;

    public MinecraftServerSnapshotIngressController(MinecraftServerSnapshotIngressHandler snapshotIngressHandler) {
        this.snapshotIngressHandler = snapshotIngressHandler;
    }

    @PostMapping("/api/frontend/minecraft/server-snapshots")
    public ResponseEntity<MinecraftServerRuntimeSnapshotResult> ingestMinecraftServerSnapshot(@RequestBody MinecraftServerRuntimeSnapshot snapshot) {
        MinecraftServerRuntimeSnapshotResult result = snapshotIngressHandler.ingest(snapshot, Instant.now());
        return result.accepted()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/api/frontend/minecraft/server-snapshots/latest")
    public ResponseEntity<MinecraftServerRuntimeSnapshot> latestMinecraftServerSnapshot(@RequestParam(required = false) String serverId) {
        return (serverId == null || serverId.isBlank()
                ? snapshotIngressHandler.latestAny()
                : snapshotIngressHandler.latest(serverId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
