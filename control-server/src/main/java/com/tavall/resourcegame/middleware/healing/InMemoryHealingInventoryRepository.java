package org.tavall.control.healing;

import org.tavall.control.identity.UniversalPlayerId;

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
