package org.tavall.control.cloud;

import java.util.Optional;

public final class CloudCommandResultHandler implements ICloudCommandResultHandler, ICloudControlDomain {
    public boolean record(CloudCommandResult result) {
        Optional<CloudCommand> existing = getCloudRepository().findCommand(result.commandId());
        if (existing.isEmpty()) {
            return false;
        }
        CloudCommandStatus status = result.success() ? CloudCommandStatus.SUCCEEDED : CloudCommandStatus.FAILED;
        getCloudRepository().saveCommand(existing.get().withStatus(status, Optional.of(redact(result.message()))));
        return true;
    }

    /**
     * Agent output is intentionally summarized before storage so command history never becomes a secret dump.
     */
    public String redact(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("(?i)(secret|token|password|apiKey)=\\S+", "$1=<redacted>")
                .replaceAll("(?i)authorization=Bearer\\s+\\S+", "authorization=<redacted>")
                .replaceAll("(?i)authorization=\\S+", "authorization=<redacted>");
    }
}
