package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public interface CitizenRepository {
    CitizenData saveCitizen(CitizenData citizen);

    Optional<CitizenData> findCitizen(CitizenId citizenId);

    List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId);

    List<CitizenData> findCitizensForKingdom(String kingdomId);

    List<CitizenData> findAllCitizens();
}
