package com.tavall.hytale.resourcegame.middleware.companion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryCompanionRepository implements CompanionRepository {
    private final ConcurrentMap<UUID, CompanionData> companionsById = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CompanionTrainingSession> trainingSessionsById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompanionWisdomUpgrade> wisdomUpgradesByKey = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CompanionWallAssignment> wallAssignmentsByCompanionId = new ConcurrentHashMap<>();

    @Override
    public CompanionData saveCompanion(CompanionData companion) {
        companionsById.put(companion.companionId(), companion);
        return companion;
    }

    @Override
    public Optional<CompanionData> findCompanion(UUID companionId) {
        return Optional.ofNullable(companionsById.get(companionId));
    }

    @Override
    public List<CompanionData> findCompanionsForPlayer(UUID ownerPlayerId) {
        ArrayList<CompanionData> companions = new ArrayList<>();
        for (CompanionData companion : companionsById.values()) {
            if (companion.ownerPlayerId().equals(ownerPlayerId)) {
                companions.add(companion);
            }
        }
        companions.sort(Comparator.comparing(CompanionData::createdAtEpochMillis));
        return List.copyOf(companions);
    }

    @Override
    public Optional<CompanionData> findActiveCompanion(UUID ownerPlayerId) {
        return findCompanionsForPlayer(ownerPlayerId).stream().findFirst();
    }

    @Override
    public CompanionTrainingSession saveTrainingSession(CompanionTrainingSession trainingSession) {
        trainingSessionsById.put(trainingSession.trainingSessionId(), trainingSession);
        return trainingSession;
    }

    @Override
    public Optional<CompanionTrainingSession> findActiveTrainingSession(UUID companionId) {
        return trainingSessionsById.values().stream()
                .filter(session -> session.companionId().equals(companionId))
                .filter(session -> !session.claimed())
                .findFirst();
    }

    @Override
    public void deleteTrainingSession(UUID trainingSessionId) {
        trainingSessionsById.remove(trainingSessionId);
    }

    @Override
    public CompanionWisdomUpgrade saveWisdomUpgrade(CompanionWisdomUpgrade wisdomUpgrade) {
        wisdomUpgradesByKey.put(wisdomKey(wisdomUpgrade.companionId(), wisdomUpgrade.skillId()), wisdomUpgrade);
        return wisdomUpgrade;
    }

    @Override
    public Optional<CompanionWisdomUpgrade> findWisdomUpgrade(UUID companionId, UUID skillId) {
        return Optional.ofNullable(wisdomUpgradesByKey.get(wisdomKey(companionId, skillId)));
    }

    @Override
    public List<CompanionWisdomUpgrade> findWisdomUpgrades(UUID companionId) {
        return wisdomUpgradesByKey.values().stream()
                .filter(upgrade -> upgrade.companionId().equals(companionId))
                .sorted(Comparator.comparing(upgrade -> upgrade.skillId().toString()))
                .toList();
    }

    @Override
    public CompanionWallAssignment saveWallAssignment(CompanionWallAssignment wallAssignment) {
        wallAssignmentsByCompanionId.put(wallAssignment.companionId(), wallAssignment);
        return wallAssignment;
    }

    @Override
    public Optional<CompanionWallAssignment> findWallAssignment(UUID companionId) {
        return Optional.ofNullable(wallAssignmentsByCompanionId.get(companionId));
    }

    @Override
    public List<CompanionWallAssignment> findWallAssignmentsForPlayer(UUID ownerPlayerId) {
        return wallAssignmentsByCompanionId.values().stream()
                .filter(assignment -> assignment.ownerPlayerId().equals(ownerPlayerId))
                .sorted(Comparator.comparing(CompanionWallAssignment::wallSectionId))
                .toList();
    }

    @Override
    public void deleteWallAssignment(UUID companionId) {
        wallAssignmentsByCompanionId.remove(companionId);
    }

    private String wisdomKey(UUID companionId, UUID skillId) {
        return companionId + ":" + skillId;
    }
}
