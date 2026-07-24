package org.tavall.control.cloud;

import org.tavall.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CloudControlPlaneConsoleTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void helpCommandListsConsoleAndDelegatedCloudCommands() {
        bootstrapRuntime();
        CloudControlPlaneConsole console = console();

        String help = console.executeCommand("help");

        assertTrue(help.contains("help"));
        assertTrue(help.contains("status"));
        assertTrue(help.contains("stop"));
        assertTrue(help.contains("cloud nodes list"));
        assertTrue(help.contains("cloud nodes register <joinToken> [hostname] [region]"));
        assertTrue(help.contains("cloud servers list"));
        assertTrue(help.contains("cloud consoles list"));
        assertTrue(help.contains("cloud consoles attach <target>"));
        assertTrue(help.contains("cloud kingdoms list"));
        assertTrue(help.contains("cloud kingdoms ensure <kingdomId>"));
    }

    @Test
    void statusCommandReportsRunningStateAndRepositoryCounts() {
        bootstrapRuntime();
        CloudControlPlaneConsole console = console();

        String status = console.executeCommand("status");

        assertTrue(status.contains("running=true"));
        assertTrue(status.contains("nodes="));
        assertTrue(status.contains("commands="));
        assertTrue(status.contains("storageRoot="));
        assertTrue(status.contains("kingdoms="));
    }

    @Test
    void cloudCommandsDelegateToCliHandler() {
        bootstrapRuntime();
        CloudControlPlaneConsole console = console();

        String result = console.executeCommand("cloud consoles list");

        assertTrue(result.contains("consoles="));
    }

    @Test
    void workloadCreateFailureReturnsErrorWithoutCrashingConsole() {
        bootstrapRuntime();
        CloudControlPlaneConsole console = console();

        String result = console.executeCommand("cloud workloads create BOT_TEST_WORKER smoke-worker");

        assertTrue(result.contains("error=No eligible cloud node."));
        assertTrue(console.isRunning());
        assertTrue(console.executeCommand("cloud consoles list").contains("consoles="));
    }

    @Test
    void localOwnerConsoleCanReconcileWorkloads() {
        bootstrapRuntime();
        CloudControlPlaneConsole console = console();

        String token = console.executeCommand("cloud nodes create-token 60")
                .substring("joinToken=".length()).trim();
        assertTrue(console.executeCommand("cloud nodes register " + token + " bootstrap-node us-west")
                .contains("success=true"));
        assertTrue(console.executeCommand("cloud workloads create BOT_TEST_WORKER smoke-worker")
                .contains("workload="));

        String reconcile = console.executeCommand("cloud workloads reconcile-all");

        assertTrue(reconcile.contains("reconciliationCommands="));
        assertFalse(reconcile.contains("error=Cloud command denied by control authority policy."));
    }

    @Test
    void stopCommandStopsTheConsoleLoop() {
        bootstrapRuntime();
        CloudControlPlaneConsole console = console();

        String stop = console.executeCommand("stop");

        assertFalse(console.isRunning());
        assertTrue(stop.contains("Stopping cloud control plane."));
    }

    private void bootstrapRuntime() {
        DependencyLoaderAccess.clear();
        try {
            DependencyLoaderAccess.registerInstance(CloudControlPlaneFilesystemLayout.class,
                    new CloudControlPlaneFilesystemLayout(Files.createTempDirectory("cloud-control-plane-console-test")));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create cloud control plane console test root", exception);
        }
        CloudControlPlaneRuntimeFactory.createInMemoryRuntime();
    }

    private CloudControlPlaneConsole console() {
        return new CloudControlPlaneConsole(new ByteArrayInputStream(new byte[0]), new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }
}
