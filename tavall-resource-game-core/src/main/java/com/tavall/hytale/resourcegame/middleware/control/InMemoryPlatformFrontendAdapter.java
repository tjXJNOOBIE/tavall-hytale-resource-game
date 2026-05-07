package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InMemoryPlatformFrontendAdapter implements PlatformFrontendAdapter {
    private final GamePlatform platform;
    private boolean connected;
    private final List<String> receivedEvents = new ArrayList<>();
    private final List<String> refreshedProjectionIds = new ArrayList<>();
    private final List<String> broadcastMessages = new ArrayList<>();

    public InMemoryPlatformFrontendAdapter(GamePlatform platform, boolean connected) {
        this.platform = platform;
        this.connected = connected;
    }

    @Override
    public GamePlatform getPlatform() {
        return platform;
    }

    @Override
    public PlatformConnectionStatus getConnectionStatus() {
        return new PlatformConnectionStatus(platform, connected, connected ? "connected" : "offline");
    }

    @Override
    public boolean supportsCommandFanout(ControlCommand command) {
        return true;
    }

    @Override
    public PlatformCommandResult refreshProjection(ControlCommand command, List<String> changedObjectIds) {
        if (!connected) {
            return failed("Platform offline; projection refresh pending.");
        }
        String projectionId = "projection:" + platform.name().toLowerCase() + ":" + command.commandId();
        refreshedProjectionIds.add(projectionId);
        return new PlatformCommandResult(platform, true, "projection refreshed", List.of(), List.of(projectionId), Map.of("changedObjectCount", Integer.toString(changedObjectIds.size())));
    }

    @Override
    public PlatformCommandResult sendControlEvent(ControlCommand command, List<String> changedObjectIds) {
        if (!connected) {
            return failed("Platform offline; control event pending.");
        }
        String eventId = "event:" + platform.name().toLowerCase() + ":" + UUID.randomUUID();
        receivedEvents.add(command.commandType().name());
        return new PlatformCommandResult(platform, true, "control event sent", List.of(eventId), List.of(), Map.of("commandType", command.commandType().name()));
    }

    @Override
    public PlatformCommandResult broadcastMessage(ControlCommand command, String message) {
        if (!connected) {
            return failed("Platform offline; broadcast pending.");
        }
        String eventId = "broadcast:" + platform.name().toLowerCase() + ":" + UUID.randomUUID();
        broadcastMessages.add(message);
        return new PlatformCommandResult(platform, true, "broadcast sent", List.of(eventId), List.of(), Map.of("messageLength", Integer.toString(message.length())));
    }

    @Override
    public PlatformCommandResult syncObject(ControlCommand command, List<String> changedObjectIds) {
        if (!connected) {
            return failed("Platform offline; sync pending.");
        }
        String eventId = "sync:" + platform.name().toLowerCase() + ":" + command.commandId();
        receivedEvents.add("SYNC");
        return new PlatformCommandResult(platform, true, "platform sync requested", List.of(eventId), List.of(), Map.of());
    }

    public List<String> receivedEvents() {
        return List.copyOf(receivedEvents);
    }

    public List<String> refreshedProjectionIds() {
        return List.copyOf(refreshedProjectionIds);
    }

    public List<String> broadcastMessages() {
        return List.copyOf(broadcastMessages);
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private PlatformCommandResult failed(String message) {
        return new PlatformCommandResult(platform, false, message, List.of(), List.of(), Map.of("pending", "true"));
    }
}
