package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.runtime.ControlOperator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CloudControlPlaneConsole implements ICloudControlDomain, IDependencyInjectableConcrete {
    private static final UUID CONSOLE_PRINCIPAL = ControlOperator.localOwner(Instant.EPOCH).operatorId();

    private final InputStream input;
    private final PrintStream output;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public CloudControlPlaneConsole() {
        this(System.in, System.out);
    }

    public CloudControlPlaneConsole(InputStream input, PrintStream output) {
        this.input = input == null ? System.in : input;
        this.output = output == null ? System.out : output;
    }

    public void run() {
        output.println("Cloud control plane console ready. Type help for commands.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        try {
            while (running.get()) {
                output.print("cloud> ");
                output.flush();
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String response = executeCommand(line);
                if (!response.isBlank()) {
                    output.print(response);
                    if (!response.endsWith(System.lineSeparator())) {
                        output.println();
                    }
                }
            }
        } catch (IOException exception) {
            output.println("Cloud control plane console I/O failed: " + exception.getMessage());
        }
    }

    public String executeCommand(String command) {
        String trimmed = command == null ? "" : command.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        try {
            if ("help".equalsIgnoreCase(trimmed)) {
                return helpText();
            }
            if ("status".equalsIgnoreCase(trimmed)) {
                return statusText();
            }
            if ("stop".equalsIgnoreCase(trimmed)) {
                running.set(false);
                return "Stopping cloud control plane." + System.lineSeparator();
            }
            if ("cloud".equals(trimmed) || trimmed.startsWith("cloud ")) {
                return getCloudControlCliHandler().execute(trimmed, CONSOLE_PRINCIPAL, Instant.now());
            }
            return "Unknown command: " + trimmed + System.lineSeparator() + helpText();
        } catch (RuntimeException exception) {
            return "error=" + exception.getMessage() + System.lineSeparator();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    private String helpText() {
        return String.join(System.lineSeparator(),
                "help",
                "status",
                "stop",
                "cloud ...",
                getCloudControlCliHandler().execute("cloud", CONSOLE_PRINCIPAL, Instant.now()).trim(),
                "");
    }

    private String statusText() {
        CloudControlPlaneFilesystemLayout layout = getCloudControlPlaneFilesystemLayout();
        return String.join(System.lineSeparator(),
                "running=" + running.get(),
                "nodes=" + getCloudRepository().findNodes().size(),
                "workloads=" + getCloudRepository().findWorkloads().size(),
                "storageRoot=" + layout.root(),
                "kingdoms=" + layout.kingdomIds().size(),
                "controlPlaneSession=" + layout.controlPlaneSessionName(),
                "commands=" + getCloudRepository().findCommands().size(),
                "alerts=" + getCloudRepository().findAlerts().size(),
                "ports=" + getCloudRepository().findPorts().size(),
                "volumes=" + getCloudRepository().findVolumes().size(),
                "");
    }
}
