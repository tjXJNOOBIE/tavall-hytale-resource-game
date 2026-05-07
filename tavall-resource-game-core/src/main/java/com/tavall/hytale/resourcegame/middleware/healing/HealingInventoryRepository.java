package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Optional;

public interface HealingInventoryRepository {
    HealingInventory saveInventory(UniversalPlayerId universalPlayerId, HealingInventory inventory);

    Optional<HealingInventory> findInventory(UniversalPlayerId universalPlayerId);
}
