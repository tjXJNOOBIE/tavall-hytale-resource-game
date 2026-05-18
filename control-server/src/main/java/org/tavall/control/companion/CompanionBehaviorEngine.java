package org.tavall.control.companion;

import org.tavall.control.runtime.ControlCommandValidationException;

import java.util.UUID;

public final class CompanionBehaviorEngine implements ICompanionDomain {
    public CompanionBehaviorEngine() {
    }

    public CompanionBehaviorEngine(CompanionRepository repository) {
        registerCompanionRepository(repository);
    }

    public CompanionData setBehaviorState(UUID companionId, CompanionBehaviorState behaviorState, long nowEpochMillis) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        CompanionStatus status = switch (behaviorState) {
            case IDLE -> CompanionStatus.IDLE;
            case FOLLOWING -> CompanionStatus.FOLLOWING;
            case AGGRESSIVE -> CompanionStatus.FOLLOWING;
            case FLEEING -> CompanionStatus.FOLLOWING;
            case DUELING -> CompanionStatus.DUELING;
        };
        return getCompanionRepository().saveCompanion(companion.withStatusAndBehavior(status, behaviorState, nowEpochMillis));
    }

    public CompanionData tickCompanionBehavior(UUID companionId, long nowEpochMillis) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (companion.moraleState() == CompanionMoraleState.POOR && companion.behaviorState() == CompanionBehaviorState.AGGRESSIVE) {
            return getCompanionRepository().saveCompanion(companion.withStatusAndBehavior(CompanionStatus.FOLLOWING, CompanionBehaviorState.FLEEING, nowEpochMillis));
        }
        return companion;
    }

    public CompanionData recallCompanion(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        return getCompanionRepository().saveCompanion(companion.withStatusAndBehavior(CompanionStatus.IDLE, CompanionBehaviorState.IDLE, nowEpochMillis));
    }

    public CompanionData summonCompanion(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        return getCompanionRepository().saveCompanion(companion.withStatusAndBehavior(CompanionStatus.FOLLOWING, CompanionBehaviorState.FOLLOWING, nowEpochMillis));
    }

    private CompanionData ownedCompanion(UUID ownerPlayerId, UUID companionId) {
        CompanionData companion = getCompanionRepository().findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (!companion.ownerPlayerId().equals(ownerPlayerId)) {
            throw new ControlCommandValidationException("Companion does not belong to this player.");
        }
        return companion;
    }
}
