package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryCitizenRepository implements CitizenRepository, IDependencyInjectableConcrete {
    private final ConcurrentMap<CitizenId, CitizenData> citizensById = new ConcurrentHashMap<>();

    @Override
    public CitizenData saveCitizen(CitizenData citizen) {
        citizensById.put(citizen.citizenId(), citizen);
        return citizen;
    }

    @Override
    public Optional<CitizenData> findCitizen(CitizenId citizenId) {
        return Optional.ofNullable(citizensById.get(citizenId));
    }

    @Override
    public List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId) {
        return citizensById.values().stream()
                .filter(citizen -> citizen.ownerPlayerId().equals(ownerPlayerId))
                .sorted(Comparator.comparing(citizen -> citizen.citizenId().value()))
                .toList();
    }

    @Override
    public List<CitizenData> findCitizensForKingdom(String kingdomId) {
        return citizensById.values().stream()
                .filter(citizen -> citizen.kingdomId().equals(kingdomId))
                .sorted(Comparator.comparing(citizen -> citizen.citizenId().value()))
                .toList();
    }

    @Override
    public List<CitizenData> findAllCitizens() {
        return citizensById.values().stream()
                .sorted(Comparator.comparing(citizen -> citizen.citizenId().value()))
                .toList();
    }
}
