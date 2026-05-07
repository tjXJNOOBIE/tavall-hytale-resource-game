package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.Optional;

public final class CloudCommandResultHandler {
    private final InMemoryCloudRepository repository;

    public CloudCommandResultHandler(InMemoryCloudRepository repository) {
        this.repository = repository;
    }

    public boolean record(CloudCommandResult result) {
        Optional<CloudCommand> existing = repository.findCommand(result.commandId());
        if (existing.isEmpty()) {
            return false;
        }
        CloudCommandStatus status = result.success() ? CloudCommandStatus.SUCCEEDED : CloudCommandStatus.FAILED;
        repository.saveCommand(existing.get().withStatus(status, Optional.of(redact(result.message()))));
        return true;
    }

    public String redact(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("(?i)(secret|token|password)=\\S+", "$1=<redacted>");
    }
}
