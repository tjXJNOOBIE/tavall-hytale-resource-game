package org.tavall.control.security;

import org.tavall.control.identity.UniversalPlayerId;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public final class TwoFactorRecoveryCodeHandler implements ISecurityDomain {
    public TwoFactorRecoveryCodeHandler() {
    }

    public TwoFactorRecoveryCodeHandler(TwoFactorRepository twoFactorRepository, TokenHasher tokenHasher, SecureRandom secureRandom) {
        registerTwoFactorRepository(twoFactorRepository);
        registerTokenHasher(tokenHasher);
        registerSecureRandom(secureRandom);
    }

    public GeneratedRecoveryCodes generateRecoveryCodes(UniversalPlayerId universalPlayerId, int count, Instant now) {
        int boundedCount = Math.max(1, Math.min(count, 16));
        List<TwoFactorRecoveryCode> stored = new ArrayList<>();
        List<String> displayCodes = new ArrayList<>();
        for (int index = 0; index < boundedCount; index++) {
            String displayCode = recoveryCode();
            TwoFactorRecoveryCode recoveryCode = new TwoFactorRecoveryCode(
                    UUID.randomUUID(),
                    universalPlayerId,
                    getTokenHasher().hashToken(displayCode),
                    java.util.Optional.empty(),
                    now
            );
            stored.add(getTwoFactorRepository().saveRecoveryCode(recoveryCode));
            displayCodes.add(displayCode);
        }
        return new GeneratedRecoveryCodes(stored, displayCodes);
    }

    public boolean consumeRecoveryCode(UniversalPlayerId universalPlayerId, String displayCode, Instant now) {
        String hash = getTokenHasher().hashToken(displayCode);
        for (TwoFactorRecoveryCode recoveryCode : getTwoFactorRepository().findRecoveryCodes(universalPlayerId)) {
            if (recoveryCode.usedAt().isEmpty() && recoveryCode.codeHash().equals(hash)) {
                getTwoFactorRepository().saveRecoveryCode(recoveryCode.used(now));
                return true;
            }
        }
        return false;
    }

    private String recoveryCode() {
        byte[] bytes = new byte[6];
        getSecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
