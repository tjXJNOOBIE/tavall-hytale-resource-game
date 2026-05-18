package org.tavall.control.healing;

import org.tavall.control.identity.UniversalPlayerId;

import java.util.Optional;

public interface HealingInventoryRepository {
    HealingInventory saveInventory(UniversalPlayerId universalPlayerId, HealingInventory inventory);

    Optional<HealingInventory> findInventory(UniversalPlayerId universalPlayerId);
}
