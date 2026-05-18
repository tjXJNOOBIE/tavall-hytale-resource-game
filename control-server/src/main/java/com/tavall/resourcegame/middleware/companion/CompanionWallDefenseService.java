package org.tavall.control.companion;

import org.tavall.control.runtime.ControlCommandValidationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CompanionWallDefenseService implements ICompanionDomain {
    public CompanionWallDefenseService() {
    }

    public CompanionWallDefenseService(CompanionRepository repository) {
        registerCompanionRepository(repository);
    }

    public CompanionWallAssignment assignCompanionToWall(UUID ownerPlayerId, UUID companionId, String wallSectionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        if (wallSectionId == null || wallSectionId.isBlank()) {
            throw new ControlCommandValidationException("Wall section ID is required.");
        }
        double bonus = calculateCompanionBonus(companion);
        CompanionWallAssignment assignment = new CompanionWallAssignment(ownerPlayerId, companionId, wallSectionId, bonus, nowEpochMillis, java.util.Map.of("source", "companion-wall"));
        CompanionRepository repository = getCompanionRepository();
        repository.saveCompanion(companion.withWallSection(Optional.of(wallSectionId), CompanionStatus.ASSIGNED_TO_WALL, nowEpochMillis));
        return repository.saveWallAssignment(assignment);
    }

    public CompanionData removeCompanionFromWall(UUID ownerPlayerId, UUID companionId, long nowEpochMillis) {
        CompanionData companion = ownedCompanion(ownerPlayerId, companionId);
        CompanionRepository repository = getCompanionRepository();
        repository.deleteWallAssignment(companionId);
        return repository.saveCompanion(companion.withWallSection(Optional.empty(), CompanionStatus.IDLE, nowEpochMillis));
    }

    public List<String> getAvailableWallSlots(UUID ownerPlayerId) {
        List<String> occupied = getCompanionRepository().findWallAssignmentsForPlayer(ownerPlayerId).stream()
                .map(CompanionWallAssignment::wallSectionId)
                .toList();
        return List.of("north", "east", "south", "west").stream()
                .filter(slot -> !occupied.contains(slot))
                .toList();
    }

    public double calculateWallCompanionBonus(UUID ownerPlayerId) {
        return getCompanionRepository().findWallAssignmentsForPlayer(ownerPlayerId).stream()
                .mapToDouble(CompanionWallAssignment::defenseBonus)
                .sum();
    }

    private double calculateCompanionBonus(CompanionData companion) {
        double base = companion.type() == CompanionType.BRUTE ? 8.0d : 4.0d;
        return base + companion.level() * 0.5d + companion.calculatedStats().earthDefense() * 0.05d;
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
