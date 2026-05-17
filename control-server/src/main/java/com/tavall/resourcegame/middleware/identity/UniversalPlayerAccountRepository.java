package com.tavall.resourcegame.middleware.identity;

import java.util.Optional;

public interface UniversalPlayerAccountRepository {
    UniversalPlayerAccount saveAccount(UniversalPlayerAccount account);

    Optional<UniversalPlayerAccount> findAccount(UniversalPlayerId universalPlayerId);
}
