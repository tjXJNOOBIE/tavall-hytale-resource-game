package org.tavall.control.identity;

import java.util.Optional;

public interface AuthIdentityRepository {
    AuthIdentity saveAuthIdentity(AuthIdentity authIdentity);

    Optional<AuthIdentity> findAuthIdentity(AuthProvider provider, String providerSubject);
}
