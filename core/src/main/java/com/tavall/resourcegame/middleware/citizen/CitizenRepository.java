package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public interface CitizenRepository extends IDependencyInjectableInterface {
    CitizenData saveCitizen(CitizenData citizen);

    Optional<CitizenData> findCitizen(CitizenId citizenId);

    List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId);

    List<CitizenData> findCitizensForKingdom(String kingdomId);

    List<CitizenData> findAllCitizens();
}
