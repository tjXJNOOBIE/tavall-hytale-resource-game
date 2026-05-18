package org.tavall.control.security;

import org.tavall.control.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TwoFactorRepository {
    TwoFactorEnrollment saveEnrollment(TwoFactorEnrollment enrollment);

    Optional<TwoFactorEnrollment> findEnrollment(UUID enrollmentId);

    Optional<TwoFactorEnrollment> findEnabledEnrollment(UniversalPlayerId universalPlayerId);

    TwoFactorRecoveryCode saveRecoveryCode(TwoFactorRecoveryCode recoveryCode);

    List<TwoFactorRecoveryCode> findRecoveryCodes(UniversalPlayerId universalPlayerId);
}
