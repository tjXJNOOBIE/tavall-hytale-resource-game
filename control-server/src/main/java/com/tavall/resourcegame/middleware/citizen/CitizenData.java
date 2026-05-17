package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.domain.CitizenJobType;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Objects;

public record CitizenData(
        CitizenId citizenId,
        UniversalPlayerId ownerPlayerId,
        String kingdomId,
        String displayName,
        long bornAtEpochMillis,
        CitizenAgeStage ageStage,
        CitizenStatus status,
        CitizenJobType jobType,
        CitizenHealthState healthState,
        CitizenMoraleState moraleState,
        CitizenHousingState housingState,
        CitizenNutritionState nutritionState,
        CitizenTrainingState trainingState,
        CitizenTroopLinkState troopLinkState,
        CitizenStatBlock statBlock,
        long createdAtEpochMillis,
        long updatedAtEpochMillis,
        long lastAgeStageProcessedAtEpochMillis,
        Map<String, String> metadata
) {
    public CitizenData {
        Objects.requireNonNull(citizenId, "citizenId");
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        if (kingdomId == null || kingdomId.isBlank()) {
            throw new IllegalArgumentException("kingdomId is required.");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required.");
        }
        Objects.requireNonNull(ageStage, "ageStage");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(jobType, "jobType");
        Objects.requireNonNull(healthState, "healthState");
        Objects.requireNonNull(moraleState, "moraleState");
        Objects.requireNonNull(housingState, "housingState");
        Objects.requireNonNull(nutritionState, "nutritionState");
        Objects.requireNonNull(trainingState, "trainingState");
        Objects.requireNonNull(troopLinkState, "troopLinkState");
        statBlock = statBlock == null ? CitizenStatBlock.balancedAdult() : statBlock;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public CitizenData withAgeStage(CitizenAgeStage newAgeStage, long nowEpochMillis) {
        return new CitizenData(citizenId, ownerPlayerId, kingdomId, displayName, bornAtEpochMillis, newAgeStage, status, jobType, healthState, moraleState, housingState, nutritionState, trainingState, troopLinkState, statBlock, createdAtEpochMillis, nowEpochMillis, nowEpochMillis, metadata);
    }

    public CitizenData withBornAtForDebug(long newBornAtEpochMillis, CitizenAgeStage newAgeStage, long nowEpochMillis) {
        return new CitizenData(citizenId, ownerPlayerId, kingdomId, displayName, newBornAtEpochMillis, newAgeStage, status, jobType, healthState, moraleState, housingState, nutritionState, trainingState, troopLinkState, statBlock, createdAtEpochMillis, nowEpochMillis, nowEpochMillis, metadata);
    }

    public CitizenData withJob(CitizenJobType newJobType, long nowEpochMillis) {
        return new CitizenData(citizenId, ownerPlayerId, kingdomId, displayName, bornAtEpochMillis, ageStage, status, newJobType, healthState, moraleState, housingState, nutritionState, trainingState, troopLinkState, statBlock, createdAtEpochMillis, nowEpochMillis, lastAgeStageProcessedAtEpochMillis, metadata);
    }

    public CitizenData withTraining(CitizenTrainingState newTrainingState, CitizenStatus newStatus, CitizenTroopLinkState newTroopLinkState, CitizenJobType newJobType, long nowEpochMillis) {
        return new CitizenData(citizenId, ownerPlayerId, kingdomId, displayName, bornAtEpochMillis, ageStage, newStatus, newJobType, healthState, moraleState, housingState, nutritionState, newTrainingState, newTroopLinkState, statBlock, createdAtEpochMillis, nowEpochMillis, lastAgeStageProcessedAtEpochMillis, metadata);
    }

    public CitizenData withConditions(CitizenHealthState health, CitizenMoraleState morale, CitizenNutritionState nutrition, CitizenHousingState housing, long nowEpochMillis) {
        return new CitizenData(citizenId, ownerPlayerId, kingdomId, displayName, bornAtEpochMillis, ageStage, status, jobType, health, morale, housing, nutrition, trainingState, troopLinkState, statBlock, createdAtEpochMillis, nowEpochMillis, lastAgeStageProcessedAtEpochMillis, metadata);
    }

    public CitizenData withMetadata(Map<String, String> newMetadata, long nowEpochMillis) {
        return new CitizenData(citizenId, ownerPlayerId, kingdomId, displayName, bornAtEpochMillis, ageStage, status, jobType, healthState, moraleState, housingState, nutritionState, trainingState, troopLinkState, statBlock, createdAtEpochMillis, nowEpochMillis, lastAgeStageProcessedAtEpochMillis, newMetadata);
    }
}
