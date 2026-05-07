package com.tavall.hytale.resourcegame.middleware.companion;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;

import java.util.UUID;

public final class CompanionBehaviorEngine {
    private final CompanionRepository repository;

    public CompanionBehaviorEngine(CompanionRepository repository) {
        this.repository = repository;
    }

    public CompanionData setBehaviorState(UUID companionId, CompanionBehaviorState behaviorState, long nowEpochMillis) {
        CompanionData companion = repository.findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        CompanionStatus status = switch (behaviorState) {
            case IDLE -> CompanionStatus.IDLE;
            case FOLLOWING -> CompanionStatus.FOLLOWING;
            case AGGRESSIVE -> CompanionStatus.FOLLOWING;
            case FLEEING -> CompanionStatus.FOLLOWING;
            case DUELING -> CompanionStatus.DUELING;
        };
        return repository.saveCompanion(companion.withStatusAndBehavior(status, behaviorState, nowEpochMillis));
    }

    public CompanionData tickCompanionBehavior(UUID companionId, long nowEpochMillis) {
        CompanionData companion = repository.findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (companion.moraleState() == CompanionMoraleState.POOR && companion.behaviorState() == CompanionBehaviorState.AGGRESSIVE) {
            return repository.saveCompanion(companion.withStatusAndBehavior(CompanionStatus.FOLLOWING, CompanionBehaviorState.FLEEING, nowEpochMillis));
        }
        return companion;
    }

    public CompanionData recallCompanion(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        return repository.saveCompanion(companion.withStatusAndBehavior(CompanionStatus.IDLE, CompanionBehaviorState.IDLE, nowEpochMillis));
    }

    public CompanionData summonCompanion(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        return repository.saveCompanion(companion.withStatusAndBehavior(CompanionStatus.FOLLOWING, CompanionBehaviorState.FOLLOWING, nowEpochMillis));
    }

    private CompanionData ownedCompanion(UUID ownerPlayerId, UUID companionId) {
        CompanionData companion = repository.findCompanion(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion was not found."));
        if (!companion.ownerPlayerId().equals(ownerPlayerId)) {
            throw new ControlCommandValidationException("Companion does not belong to this player.");
        }
        return companion;
    }
}
