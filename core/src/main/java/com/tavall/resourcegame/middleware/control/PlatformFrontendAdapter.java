package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.List;

public interface PlatformFrontendAdapter {
    GamePlatform getPlatform();

    PlatformConnectionStatus getConnectionStatus();

    boolean supportsCommandFanout(ControlCommand command);

    PlatformCommandResult refreshProjection(ControlCommand command, List<String> changedObjectIds);

    PlatformCommandResult sendControlEvent(ControlCommand command, List<String> changedObjectIds);

    PlatformCommandResult broadcastMessage(ControlCommand command, String message);

    PlatformCommandResult syncObject(ControlCommand command, List<String> changedObjectIds);
}
