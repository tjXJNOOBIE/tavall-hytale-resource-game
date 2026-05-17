package com.tavall.resourcegame.middleware.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecordingControlSurfaceLaunchHandler implements ControlSurfaceLaunchHandler {
    private final ArrayList<ControlSurfaceLaunchResult> launchResults = new ArrayList<>();

    @Override
    public ControlSurfaceLaunchResult startSurface(String surfaceName, Map<String, String> arguments) {
        ControlSurfaceLaunchResult result = new ControlSurfaceLaunchResult(
                true,
                "Control surface launch requested: " + surfaceName + ".",
                "recorded-" + surfaceName,
                Map.copyOf(arguments)
        );
        launchResults.add(result);
        return result;
    }

    public List<ControlSurfaceLaunchResult> launchResults() {
        return List.copyOf(launchResults);
    }
}
