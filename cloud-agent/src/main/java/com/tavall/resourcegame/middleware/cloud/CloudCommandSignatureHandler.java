package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class CloudCommandSignatureHandler implements ICloudCommandSignatureHandler, ICloudAgentDomain, IDependencyInjectableConcrete {
    @Override
    public boolean isSignatureRequired() {
        return getCloudAgentSecurityConfig().commandSigningRequired();
    }

    @Override
    public boolean verify(CloudCommand command) {
        if (!isSignatureRequired()) {
            return true;
        }
        if (command.signature().isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(
                command.signature().orElseThrow().getBytes(StandardCharsets.UTF_8),
                signatureFor(command).getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String signatureFor(CloudCommand command) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(getCloudAgentSecurityConfig().commandSigningSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonicalPayload(command).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign cloud command payload.", exception);
        }
    }

    /**
     * The command signature excludes the signature field itself so control-plane and agent code can compute the same digest.
     */
    private String canonicalPayload(CloudCommand command) {
        return command.commandId()
                + "|" + command.nodeId()
                + "|" + (command.commandType() == null ? "" : command.commandType().name())
                + "|" + command.payloadJson()
                + "|" + command.requestedBy()
                + "|" + command.requestedAt()
                + "|" + command.correlationId()
                + "|" + command.expiresAt().map(Object::toString).orElse("");
    }
}
