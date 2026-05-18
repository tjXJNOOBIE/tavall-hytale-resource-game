package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.identity.UniversalPlayerId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CitizenCreationHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenCreationHandler() {
    }

    public CitizenCreationHandler(
            CitizenRepository citizenRepository,
            CitizenAgingCalculationHandler agingCalculationHandler,
            CitizenCacheInvalidationHandler cacheInvalidationHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenAgingCalculationHandler.class, agingCalculationHandler);
        registerCitizenDependency(CitizenCacheInvalidationHandler.class, cacheInvalidationHandler);
    }

    public CitizenData createCitizen(UniversalPlayerId ownerPlayerId, String kingdomId, String displayName, long bornAtEpochMillis, long nowEpochMillis, CitizenAgingConfig config) {
        CitizenData candidate = new CitizenData(
                CitizenId.random(),
                ownerPlayerId,
                kingdomId,
                displayName,
                bornAtEpochMillis,
                CitizenAgeStage.YOUNG_ADULT,
                CitizenStatus.ACTIVE_CITIZEN,
                CitizenJobType.IDLE,
                CitizenHealthState.HEALTHY,
                CitizenMoraleState.MEDIUM,
                CitizenHousingState.UNKNOWN,
                CitizenNutritionState.UNKNOWN,
                CitizenTrainingState.UNTRAINED,
                CitizenTroopLinkState.NOT_TROOP,
                CitizenStatBlock.balancedAdult(),
                nowEpochMillis,
                nowEpochMillis,
                nowEpochMillis,
                Map.of("canonicalOwner", "plain-java-control-server")
        );
        CitizenAgeStage stage = getCitizenAgingCalculationHandler().calculateAgeStage(candidate, nowEpochMillis, config);
        CitizenData created = getCitizenRepository().saveCitizen(candidate.withAgeStage(stage, nowEpochMillis));
        getCitizenCacheInvalidationHandler().invalidateCitizenScopes(created);
        return created;
    }

    public List<CitizenData> createCitizens(UniversalPlayerId ownerPlayerId, String kingdomId, int amount, long nowEpochMillis, CitizenAgingConfig config) {
        if (amount <= 0) {
            throw new CitizenValidationException("Citizen amount must be positive.");
        }
        ArrayList<CitizenData> created = new ArrayList<>();
        long defaultAdultBornAt = nowEpochMillis - Math.round(20 * (double) config.gameDaysPerYear() * config.realMillisPerGameDay());
        for (int index = 0; index < amount; index++) {
            created.add(createCitizen(ownerPlayerId, kingdomId, "Citizen " + (index + 1), defaultAdultBornAt, nowEpochMillis, config));
        }
        return List.copyOf(created);
    }
}
