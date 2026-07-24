package org.tavall.control.population;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.population.UpgradeActionState;

import java.time.Instant;
import java.util.UUID;

public interface IPopulationHandler extends IDependencyInjectableInterface {
    PlayerGameState addCitizens(UUID playerId, int amount);

    PlayerGameState setCitizens(UUID playerId, int count);

    PlayerGameState addTroops(UUID playerId, int amount);

    PlayerGameState setTroops(UUID playerId, int count);

    boolean promoteCitizen(UUID playerId);

    boolean demoteTroop(UUID playerId);

    UpgradeActionState promoteActionState(PlayerGameState state);

    UpgradeActionState demoteActionState(PlayerGameState state);

    String promotionCostSummary(PlayerGameState state);

    PlayerGameState updateAging(UUID playerId, Instant now);
}
