package com.tavall.resourcegame.distribution.remote;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.distribution.IDistributionDomain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class RemoteCommandHandler implements IRemoteCommandHandler, IDistributionDomain, IDependencyInjectableConcrete {
    private static final Pattern PRIVATE_KEY_BLOCK = Pattern.compile("-----BEGIN [A-Z ]*PRIVATE KEY-----.*?-----END [A-Z ]*PRIVATE KEY-----", Pattern.DOTALL);

    public RemoteCommandResult runRemoteCommand(RemoteTarget target, RemoteCommand command) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(command, "command");
        Instant startedAt = Instant.now();
        List<String> validationErrors = getRemoteCommandPolicy().validate(command);
        if (!validationErrors.isEmpty()) {
            return new RemoteCommandResult(
                    command.commandId(),
                    target.targetId(),
                    false,
                    false,
                    -1,
                    "",
                    String.join(" ", validationErrors),
                    startedAt,
                    Instant.now(),
                    Map.of("rejectedByPolicy", "true")
            );
        }

        List<String> processCommand = processCommand(target, command);
        Process process = null;
        try {
            process = new ProcessBuilder(processCommand).start();
            Process runningProcess = process;
            CompletableFuture<String> stdout = CompletableFuture.supplyAsync(() -> readAll(runningProcess.getInputStream()));
            CompletableFuture<String> stderr = CompletableFuture.supplyAsync(() -> readAll(runningProcess.getErrorStream()));
            boolean completed = process.waitFor(command.timeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new RemoteCommandResult(command.commandId(), target.targetId(), false, true, -1, sanitize(stdout.join()), sanitize(stderr.join()), startedAt, Instant.now(), Map.of("commandTimedOut", "true"));
            }
            int exitCode = process.exitValue();
            return new RemoteCommandResult(command.commandId(), target.targetId(), exitCode == 0, false, exitCode, sanitize(stdout.join()), sanitize(stderr.join()), startedAt, Instant.now(), Map.of());
        } catch (Exception ex) {
            if (process != null) {
                process.destroyForcibly();
            }
            return new RemoteCommandResult(command.commandId(), target.targetId(), false, false, -1, "", sanitize(ex.getMessage()), startedAt, Instant.now(), Map.of("exception", ex.getClass().getSimpleName()));
        }
    }

    private List<String> processCommand(RemoteTarget target, RemoteCommand command) {
        List<String> processCommand = new ArrayList<>();
        if (!target.localTarget()) {
            processCommand.add("ssh");
            processCommand.add(target.connectHost());
        }
        processCommand.add(command.executable());
        processCommand.addAll(command.arguments());
        return processCommand;
    }

    private String readAll(java.io.InputStream inputStream) {
        try (inputStream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitized = PRIVATE_KEY_BLOCK.matcher(value).replaceAll("[redacted-private-key]");
        sanitized = sanitized.replaceAll("(?i)(password|secret|token)=\\S+", "$1=[redacted]");
        return sanitized;
    }
}
