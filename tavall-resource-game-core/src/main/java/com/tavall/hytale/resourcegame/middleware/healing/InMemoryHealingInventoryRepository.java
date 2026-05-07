package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryHealingInventoryRepository implements HealingInventoryRepository {
    private final Map<UniversalPlayerId, HealingInventory> inventoriesByPlayer = new ConcurrentHashMap<>();

    @Override
    public HealingInventory saveInventory(UniversalPlayerId universalPlayerId, HealingInventory inventory) {
        inventoriesByPlayer.put(universalPlayerId, inventory);
        return inventory;
    }

    @Override
    public Optional<HealingInventory> findInventory(UniversalPlayerId universalPlayerId) {
        return Optional.ofNullable(inventoriesByPlayer.get(universalPlayerId));
    }
}
