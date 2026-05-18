package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import org.tavall.control.identity.UniversalPlayerId;

import java.util.List;
import java.util.Optional;

public interface CitizenRepository extends IDependencyInjectableInterface {
    CitizenData saveCitizen(CitizenData citizen);

    Optional<CitizenData> findCitizen(CitizenId citizenId);

    List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId);

    List<CitizenData> findCitizensForKingdom(String kingdomId);

    List<CitizenData> findAllCitizens();
}
