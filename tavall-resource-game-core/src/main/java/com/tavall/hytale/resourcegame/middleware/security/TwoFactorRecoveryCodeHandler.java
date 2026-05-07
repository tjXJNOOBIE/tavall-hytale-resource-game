package com.tavall.hytale.resourcegame.middleware.security;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public final class TwoFactorRecoveryCodeHandler {
    private final TwoFactorRepository twoFactorRepository;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom;

    public TwoFactorRecoveryCodeHandler(TwoFactorRepository twoFactorRepository, TokenHasher tokenHasher, SecureRandom secureRandom) {
        this.twoFactorRepository = twoFactorRepository;
        this.tokenHasher = tokenHasher;
        this.secureRandom = secureRandom;
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
                    tokenHasher.hashToken(displayCode),
                    java.util.Optional.empty(),
                    now
            );
            stored.add(twoFactorRepository.saveRecoveryCode(recoveryCode));
            displayCodes.add(displayCode);
        }
        return new GeneratedRecoveryCodes(stored, displayCodes);
    }

    public boolean consumeRecoveryCode(UniversalPlayerId universalPlayerId, String displayCode, Instant now) {
        String hash = tokenHasher.hashToken(displayCode);
        for (TwoFactorRecoveryCode recoveryCode : twoFactorRepository.findRecoveryCodes(universalPlayerId)) {
            if (recoveryCode.usedAt().isEmpty() && recoveryCode.codeHash().equals(hash)) {
                twoFactorRepository.saveRecoveryCode(recoveryCode.used(now));
                return true;
            }
        }
        return false;
    }

    private String recoveryCode() {
        byte[] bytes = new byte[6];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
