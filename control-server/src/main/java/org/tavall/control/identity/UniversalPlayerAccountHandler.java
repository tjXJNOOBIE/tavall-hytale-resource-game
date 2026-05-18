package org.tavall.control.identity;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class UniversalPlayerAccountHandler implements IIdentityDomain {
    public UniversalPlayerAccountHandler() {
    }

    public UniversalPlayerAccountHandler(UniversalPlayerAccountRepository accountRepository) {
        registerUniversalPlayerAccountRepository(accountRepository);
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
        return getUniversalPlayerAccountRepository().saveAccount(account);
    }
}
