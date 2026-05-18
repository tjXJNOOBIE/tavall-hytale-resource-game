package org.tavall.control.troop;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.castle.CastleId;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.common.MetadataMaps;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Troop(
        TroopId troopId,
        Optional<UniversalPlayerId> ownerPlayerId,
        Optional<GuildId> ownerGuildId,
        String troopType,
        int tier,
        int health,
        TroopStatus status,
        CanonicalLocation location,
        Optional<CastleId> assignedCastleId,
        GlobalAssetId globalAssetId,
        Map<String, String> metadata
) {
    public Troop {
        Objects.requireNonNull(troopId, "troopId");
        ownerPlayerId = ownerPlayerId == null ? Optional.empty() : ownerPlayerId;
        ownerGuildId = ownerGuildId == null ? Optional.empty() : ownerGuildId;
        troopType = troopType == null || troopType.isBlank() ? "infantry" : troopType;
        if (tier < 1 || tier > 10) {
            throw new TroopValidationException("Troop tier must be 1-10.");
        }
        health = Math.max(0, health);
        status = status == null ? TroopStatus.IDLE : status;
        Objects.requireNonNull(location, "location");
        assignedCastleId = assignedCastleId == null ? Optional.empty() : assignedCastleId;
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        metadata = MetadataMaps.immutable(metadata);
    }

    public Troop withStatus(TroopStatus status) {
        return new Troop(troopId, ownerPlayerId, ownerGuildId, troopType, tier, health, status, location, assignedCastleId, globalAssetId, metadata);
    }
}
