package org.tavall.control.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LocalProcessControlSurfaceLaunchHandler implements ControlSurfaceLaunchHandler {
    private static final String WEB_PANEL_MAIN_CLASS = "org.tavall.control.web.ControlServerApplication";

    @Override
    public ControlSurfaceLaunchResult startSurface(String surfaceName, Map<String, String> arguments) {
        if (!surfaceName.equals("web-panel")) {
            return new ControlSurfaceLaunchResult(false, "Unsupported control surface: " + surfaceName + ".", "", Map.of());
        }
        String port = arguments.getOrDefault("port", "8080");
        try {
            Path logDirectory = Path.of(".control-surfaces").toAbsolutePath();
            File logDirectoryFile = logDirectory.toFile();
            if (!logDirectoryFile.exists() && !logDirectoryFile.mkdirs()) {
                return new ControlSurfaceLaunchResult(false, "Unable to create control surface log directory: " + logDirectory + ".", "", Map.of());
            }
            ProcessBuilder processBuilder = new ProcessBuilder(webPanelCommand(port));
            processBuilder.redirectOutput(logDirectory.resolve("web-panel.out.log").toFile());
            processBuilder.redirectError(logDirectory.resolve("web-panel.err.log").toFile());
            Process process = processBuilder.start();
            LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
            metadata.put("surface", "web-panel");
            metadata.put("port", port);
            metadata.put("pid", Long.toString(process.pid()));
            metadata.put("outLog", logDirectory.resolve("web-panel.out.log").toString());
            metadata.put("errLog", logDirectory.resolve("web-panel.err.log").toString());
            return new ControlSurfaceLaunchResult(true, "Web control panel launch requested on port " + port + ".", Long.toString(process.pid()), metadata);
        } catch (IOException exception) {
            return new ControlSurfaceLaunchResult(false, "Web control panel launch failed: " + exception.getMessage(), "", Map.of("surface", "web-panel", "port", port));
        }
    }

    private ArrayList<String> webPanelCommand(String port) {
        ArrayList<String> command = new ArrayList<>();
        command.add(javaBinary());
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(WEB_PANEL_MAIN_CLASS);
        command.add("--server.port=" + port);
        return command;
    }

    private String javaBinary() {
        String javaHome = System.getProperty("java.home");
        String executableName = System.getProperty("os.name", "").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(javaHome, "bin", executableName).toString();
    }
}
