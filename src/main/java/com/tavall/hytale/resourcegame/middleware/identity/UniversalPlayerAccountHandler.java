package com.tavall.hytale.resourcegame.middleware.identity;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class UniversalPlayerAccountHandler {
    private final UniversalPlayerAccountRepository accountRepository;

    public UniversalPlayerAccountHandler(UniversalPlayerAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UniversalPlayerAccount createUniversalAccount(String displayName, Optional<String> primaryEmail, Instant now) {
        UniversalPlayerAccount account = new UniversalPlayerAccount(
                UniversalPlayerId.random(),
                displayName,
                primaryEmail,
                now,
                now,
                false,
                Map.of()
        );
        return accountRepository.saveAccount(account);
    }
}
