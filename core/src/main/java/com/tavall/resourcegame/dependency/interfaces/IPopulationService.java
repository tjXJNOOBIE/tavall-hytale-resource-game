package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.ui.UpgradeActionState;

import java.time.Instant;
import java.util.UUID;

public interface IPopulationService extends IDependencyInjectableInterface {
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
