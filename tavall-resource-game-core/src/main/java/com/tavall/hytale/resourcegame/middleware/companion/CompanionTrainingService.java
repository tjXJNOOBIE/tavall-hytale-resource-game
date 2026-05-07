package com.tavall.hytale.resourcegame.middleware.companion;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public final class CompanionTrainingService {
    private final CompanionRepository repository;
    private final CompanionStatsService statsService;
    private final CompanionLevelingService levelingService;

    public CompanionTrainingService(CompanionRepository repository, CompanionStatsService statsService, CompanionLevelingService levelingService) {
        this.repository = repository;
        this.statsService = statsService;
        this.levelingService = levelingService;
    }

    public CompanionTrainingSession startCompanionTraining(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        if (companion.status() == CompanionStatus.TRAINING) {
            throw new ControlCommandValidationException("Companion is already training.");
        }
        repository.findActiveTrainingSession(companionId).ifPresent(session -> {
            throw new ControlCommandValidationException("Companion already has an active training session.");
        });
        long durationMillis = calculateTrainingDuration(companion).toMillis();
        long xp = calculateTrainingXp(companion, durationMillis);
        CompanionTrainingSession session = new CompanionTrainingSession(UUID.randomUUID(), companionId, ownerPlayerId, nowEpochMillis, nowEpochMillis + durationMillis, xp, false, Map.of("source", "companion-camp"));
        repository.saveCompanion(companion.withStatusAndBehavior(CompanionStatus.TRAINING, CompanionBehaviorState.IDLE, nowEpochMillis));
        return repository.saveTrainingSession(session);
    }

    public CompanionData claimCompanionTraining(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        CompanionTrainingSession session = repository.findActiveTrainingSession(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion has no active training session."));
        long elapsed = Math.max(0, Math.min(nowEpochMillis, session.expectedCompletedAtEpochMillis()) - session.startedAtEpochMillis());
        long total = Math.max(1, session.expectedCompletedAtEpochMillis() - session.startedAtEpochMillis());
        long earnedXp = session.expectedXp() * elapsed / total;
        CompanionData updated = applyXp(companion, earnedXp, nowEpochMillis)
                .withStatusAndBehavior(CompanionStatus.IDLE, CompanionBehaviorState.IDLE, nowEpochMillis);
        repository.deleteTrainingSession(session.trainingSessionId());
        return repository.saveCompanion(updated);
    }

    public CompanionData cancelCompanionTraining(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        return claimCompanionTraining(ownerPlayerId, companionId, nowEpochMillis);
    }

    public CompanionTrainingSession applyCompanionTrainingSpeedup(UUID ownerPlayerId, UUID companionId, double speedupRatio, long nowEpochMillis) {
        ownedCompanion(ownerPlayerId, companionId);
        CompanionTrainingSession session = repository.findActiveTrainingSession(companionId)
                .orElseThrow(() -> new ControlCommandValidationException("Companion has no active training session."));
        long remaining = Math.max(0, session.expectedCompletedAtEpochMillis() - nowEpochMillis);
        long reducedRemaining = Math.round(remaining * Math.max(0.0d, 1.0d - speedupRatio));
        CompanionTrainingSession updated = new CompanionTrainingSession(session.trainingSessionId(), session.companionId(), session.ownerPlayerId(), session.startedAtEpochMillis(), nowEpochMillis + reducedRemaining, session.expectedXp(), session.claimed(), session.metadata());
        return repository.saveTrainingSession(updated);
    }

    public Duration calculateTrainingDuration(CompanionData companion) {
        long minutes = 20L + companion.level() * 2L;
        return Duration.ofMinutes(minutes);
    }

    public long calculateTrainingXp(CompanionData companion, long durationMillis) {
        long minutes = Math.max(1, Duration.ofMillis(durationMillis).toMinutes());
        double moraleModifier = switch (companion.moraleState()) {
            case HIGH -> 1.2d;
            case MEDIUM -> 1.0d;
            case LOW -> 0.75d;
            case POOR -> 0.4d;
        };
        return Math.round(minutes * 8.0d * moraleModifier);
    }

    public CompanionData applyXp(CompanionData companion, long xp, long nowEpochMillis) {
        long newXp = Math.max(0, companion.xp() + xp);
        int newLevel = levelingService.levelForXp(newXp);
        CompanionData withProgress = companion.withProgress(newLevel, newXp, companion.calculatedStats(), nowEpochMillis);
        CompanionData withStats = withProgress.withProgress(newLevel, newXp, statsService.calculateCompanionStats(withProgress), nowEpochMillis);
        return withStats.withSkillSlots(levelingService.unlockSlotsForLevel(withStats.skillSlots(), newLevel), nowEpochMillis);
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
