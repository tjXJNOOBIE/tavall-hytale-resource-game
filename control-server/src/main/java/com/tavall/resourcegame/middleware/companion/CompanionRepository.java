package org.tavall.control.companion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanionRepository {
    CompanionData saveCompanion(CompanionData companion);

    Optional<CompanionData> findCompanion(UUID companionId);

    List<CompanionData> findCompanionsForPlayer(UUID ownerPlayerId);

    Optional<CompanionData> findActiveCompanion(UUID ownerPlayerId);

    CompanionTrainingSession saveTrainingSession(CompanionTrainingSession trainingSession);

    Optional<CompanionTrainingSession> findActiveTrainingSession(UUID companionId);

    void deleteTrainingSession(UUID trainingSessionId);

    CompanionWisdomUpgrade saveWisdomUpgrade(CompanionWisdomUpgrade wisdomUpgrade);

    Optional<CompanionWisdomUpgrade> findWisdomUpgrade(UUID companionId, UUID skillId);

    List<CompanionWisdomUpgrade> findWisdomUpgrades(UUID companionId);

    CompanionWallAssignment saveWallAssignment(CompanionWallAssignment wallAssignment);

    Optional<CompanionWallAssignment> findWallAssignment(UUID companionId);

    List<CompanionWallAssignment> findWallAssignmentsForPlayer(UUID ownerPlayerId);

    void deleteWallAssignment(UUID companionId);
}
