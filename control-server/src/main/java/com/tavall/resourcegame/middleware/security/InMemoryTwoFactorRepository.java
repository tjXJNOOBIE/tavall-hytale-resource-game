package com.tavall.resourcegame.middleware.security;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTwoFactorRepository implements TwoFactorRepository {
    private final Map<UUID, TwoFactorEnrollment> enrollmentsById = new ConcurrentHashMap<>();
    private final Map<UUID, TwoFactorRecoveryCode> recoveryCodesById = new ConcurrentHashMap<>();

    @Override
    public TwoFactorEnrollment saveEnrollment(TwoFactorEnrollment enrollment) {
        enrollmentsById.put(enrollment.enrollmentId(), enrollment);
        return enrollment;
    }

    @Override
    public Optional<TwoFactorEnrollment> findEnrollment(UUID enrollmentId) {
        return Optional.ofNullable(enrollmentsById.get(enrollmentId));
    }

    @Override
    public Optional<TwoFactorEnrollment> findEnabledEnrollment(UniversalPlayerId universalPlayerId) {
        return enrollmentsById.values().stream()
                .filter(enrollment -> enrollment.universalPlayerId().equals(universalPlayerId))
                .filter(TwoFactorEnrollment::enabled)
                .findFirst();
    }

    @Override
    public TwoFactorRecoveryCode saveRecoveryCode(TwoFactorRecoveryCode recoveryCode) {
        recoveryCodesById.put(recoveryCode.recoveryCodeId(), recoveryCode);
        return recoveryCode;
    }

    @Override
    public List<TwoFactorRecoveryCode> findRecoveryCodes(UniversalPlayerId universalPlayerId) {
        List<TwoFactorRecoveryCode> recoveryCodes = new ArrayList<>();
        for (TwoFactorRecoveryCode recoveryCode : recoveryCodesById.values()) {
            if (recoveryCode.universalPlayerId().equals(universalPlayerId)) {
                recoveryCodes.add(recoveryCode);
            }
        }
        return List.copyOf(recoveryCodes);
    }
}
