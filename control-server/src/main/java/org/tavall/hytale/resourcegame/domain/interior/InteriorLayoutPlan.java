package org.tavall.minecraft.domain.interior;

import org.tavall.minecraft.runtime.WorldPosition;

public record InteriorLayoutPlan(
    WorldPosition citizenAnchorPosition,
    WorldPosition troopAnchorPosition,
    WorldPosition returnPortalPosition,
    WorldPosition futureUpgradeZone,
    WorldPosition futureStationZone
) {
}
